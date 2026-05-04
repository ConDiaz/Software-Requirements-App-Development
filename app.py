from flask import Flask, render_template, request, jsonify, session, redirect, url_for
import uuid
import time
import random
from datetime import datetime

app = Flask(__name__)
app.secret_key = "gaming_platform_secret"

# ─── In-memory "database" ────────────────────────────────────────────────────
GAME_LIBRARY = {
    "game-001": {"title": "CrossFire Arena", "version": "2.4.1", "genre": "Action"},
    "game-002": {"title": "Quest Realms",    "version": "1.8.0", "genre": "RPG"},
    "game-003": {"title": "TacticsX",        "version": "3.1.2", "genre": "Strategy"},
}

PLAYERS = {
    "player-001": {
        "playerID": "player-001", "username": "Alex_PC",     "platform": "PC",
        "owned_games": ["game-001", "game-002"], "game_versions": {"game-001": "2.4.1", "game-002": "1.8.0"}
    },
    "player-002": {
        "playerID": "player-002", "username": "Maria_PS5",   "platform": "PlayStation",
        "owned_games": ["game-001", "game-003"], "game_versions": {"game-001": "2.4.1", "game-003": "3.1.2"}
    },
    "player-003": {
        "playerID": "player-003", "username": "David_Xbox",  "platform": "Xbox",
        "owned_games": ["game-001", "game-002"], "game_versions": {"game-001": "2.3.0", "game-002": "1.8.0"}  # outdated version!
    },
    "player-004": {
        "playerID": "player-004", "username": "Sam_PC",      "platform": "PC",
        "owned_games": ["game-002", "game-003"], "game_versions": {"game-002": "1.8.0", "game-003": "3.1.2"}
    },
}

LOBBIES = {}       # lobbyID -> lobby dict
CHAT_LOGS = {}     # lobbyID -> list of messages
SERVER_STATUS = {"latency": 42, "status": "stable", "region": "US-Central"}

# ─── Routes ──────────────────────────────────────────────────────────────────

@app.route("/")
def index():
    return render_template("index.html", players=PLAYERS)


@app.route("/login", methods=["POST"])
def login():
    player_id = request.form.get("player_id")
    if player_id in PLAYERS:
        session["player_id"] = player_id
        session["username"] = PLAYERS[player_id]["username"]
        return redirect(url_for("dashboard"))
    return render_template("index.html", players=PLAYERS, error="Player not found.")


@app.route("/dashboard")
def dashboard():
    if "player_id" not in session:
        return redirect(url_for("index"))
    player = PLAYERS[session["player_id"]]
    my_lobbies = {lid: l for lid, l in LOBBIES.items() if session["player_id"] in l["players"]}
    open_lobbies = {lid: l for lid, l in LOBBIES.items() if l["status"] == "waiting" and session["player_id"] not in l["players"]}
    return render_template("dashboard.html", player=player, game_library=GAME_LIBRARY,
                           my_lobbies=my_lobbies, open_lobbies=open_lobbies,
                           server=SERVER_STATUS)


# ─── USE CASE 1: Verify Game Ownership & Compatibility ───────────────────────

@app.route("/verify-ownership", methods=["POST"])
def verify_ownership():
    data = request.json
    game_id = data.get("game_id")
    player_ids = data.get("player_ids", [])

    if game_id not in GAME_LIBRARY:
        return jsonify({"success": False, "error": "Game not found."})

    required_version = GAME_LIBRARY[game_id]["version"]
    results = []
    all_clear = True

    for pid in player_ids:
        if pid not in PLAYERS:
            results.append({"player": pid, "status": "not_found", "message": "Player does not exist."})
            all_clear = False
            continue
        p = PLAYERS[pid]
        if game_id not in p["owned_games"]:
            results.append({"player": p["username"], "platform": p["platform"],
                            "status": "not_owned", "message": f"{p['username']} does not own this game."})
            all_clear = False
        elif p["game_versions"].get(game_id) != required_version:
            player_ver = p["game_versions"].get(game_id, "unknown")
            results.append({"player": p["username"], "platform": p["platform"],
                            "status": "outdated",
                            "message": f"{p['username']} has v{player_ver} — required v{required_version}. Update needed."})
            all_clear = False
        else:
            results.append({"player": p["username"], "platform": p["platform"],
                            "status": "ok", "message": f"{p['username']} ({p['platform']}) — verified ✓"})

    return jsonify({"success": all_clear, "game": GAME_LIBRARY[game_id]["title"],
                    "required_version": required_version, "results": results})


# ─── USE CASE 2: Create / Join Shared Lobby ──────────────────────────────────

@app.route("/create-lobby", methods=["POST"])
def create_lobby():
    if "player_id" not in session:
        return jsonify({"success": False, "error": "Not logged in."})
    data = request.json
    game_id = data.get("game_id")
    max_players = int(data.get("max_players", 4))

    if game_id not in GAME_LIBRARY:
        return jsonify({"success": False, "error": "Invalid game selected."})

    lobby_id = str(uuid.uuid4())[:8].upper()
    LOBBIES[lobby_id] = {
        "lobbyID": lobby_id,
        "game_id": game_id,
        "game_title": GAME_LIBRARY[game_id]["title"],
        "host": session["player_id"],
        "host_name": session["username"],
        "players": [session["player_id"]],
        "player_names": [session["username"]],
        "max_players": max_players,
        "status": "waiting",
        "created_at": datetime.now().strftime("%H:%M:%S"),
    }
    CHAT_LOGS[lobby_id] = []
    return jsonify({"success": True, "lobby_id": lobby_id, "lobby": LOBBIES[lobby_id]})


@app.route("/join-lobby", methods=["POST"])
def join_lobby():
    if "player_id" not in session:
        return jsonify({"success": False, "error": "Not logged in."})
    data = request.json
    lobby_id = data.get("lobby_id", "").upper()

    if lobby_id not in LOBBIES:
        return jsonify({"success": False, "error": f"Lobby '{lobby_id}' not found."})

    lobby = LOBBIES[lobby_id]
    pid = session["player_id"]

    if lobby["status"] != "waiting":
        return jsonify({"success": False, "error": "Lobby is no longer accepting players."})
    if pid in lobby["players"]:
        return jsonify({"success": False, "error": "You are already in this lobby."})
    if len(lobby["players"]) >= lobby["max_players"]:
        return jsonify({"success": False, "error": "Lobby is full."})

    # Check ownership before joining
    p = PLAYERS[pid]
    game_id = lobby["game_id"]
    if game_id not in p["owned_games"]:
        return jsonify({"success": False,
                        "error": f"You do not own '{lobby['game_title']}'. Cannot join."})

    lobby["players"].append(pid)
    lobby["player_names"].append(session["username"])
    CHAT_LOGS[lobby_id].append({"sender": "System", "message": f"{session['username']} joined the lobby.", "time": datetime.now().strftime("%H:%M")})
    return jsonify({"success": True, "lobby": lobby})


@app.route("/lobby/<lobby_id>")
def lobby_view(lobby_id):
    if "player_id" not in session:
        return redirect(url_for("index"))
    lobby_id = lobby_id.upper()
    if lobby_id not in LOBBIES:
        return redirect(url_for("dashboard"))
    lobby = LOBBIES[lobby_id]
    messages = CHAT_LOGS.get(lobby_id, [])
    player = PLAYERS[session["player_id"]]
    all_players_info = [PLAYERS[pid] for pid in lobby["players"] if pid in PLAYERS]
    return render_template("lobby.html", lobby=lobby, messages=messages,
                           player=player, all_players=all_players_info)


@app.route("/start-game", methods=["POST"])
def start_game():
    data = request.json
    lobby_id = data.get("lobby_id", "").upper()
    if lobby_id not in LOBBIES:
        return jsonify({"success": False, "error": "Lobby not found."})
    lobby = LOBBIES[lobby_id]
    if lobby["host"] != session.get("player_id"):
        return jsonify({"success": False, "error": "Only the host can start the game."})
    if len(lobby["players"]) < 2:
        return jsonify({"success": False, "error": "Need at least 2 players to start."})
    lobby["status"] = "active"
    CHAT_LOGS[lobby_id].append({"sender": "System", "message": "Game started! Good luck!", "time": datetime.now().strftime("%H:%M")})
    return jsonify({"success": True})


# ─── USE CASE 3: Cross-Platform Text Chat ────────────────────────────────────

@app.route("/send-message", methods=["POST"])
def send_message():
    if "player_id" not in session:
        return jsonify({"success": False, "error": "Not logged in."})
    data = request.json
    lobby_id = data.get("lobby_id", "").upper()
    message = data.get("message", "").strip()

    if not message:
        return jsonify({"success": False, "error": "Message cannot be empty."})
    if len(message) > 200:
        return jsonify({"success": False, "error": "Message too long (max 200 chars)."})
    if lobby_id not in CHAT_LOGS:
        return jsonify({"success": False, "error": "Lobby not found."})

    entry = {
        "sender": session["username"],
        "platform": PLAYERS[session["player_id"]]["platform"],
        "message": message,
        "time": datetime.now().strftime("%H:%M")
    }
    CHAT_LOGS[lobby_id].append(entry)
    return jsonify({"success": True, "entry": entry})


@app.route("/get-messages/<lobby_id>")
def get_messages(lobby_id):
    lobby_id = lobby_id.upper()
    return jsonify({"messages": CHAT_LOGS.get(lobby_id, [])})


# ─── Server Status (latency simulation) ─────────────────────────────────────

@app.route("/server-status")
def server_status():
    # Simulate latency variation
    base = SERVER_STATUS["latency"]
    current = base + random.randint(-10, 80)
    status = "stable"
    warning = None
    if current > 150:
        status = "degraded"
        warning = "High latency detected. Network optimization in progress..."
    elif current > 100:
        status = "warning"
        warning = "Latency above optimal threshold."
    return jsonify({"latency": current, "status": status, "warning": warning,
                    "region": SERVER_STATUS["region"]})


@app.route("/logout")
def logout():
    session.clear()
    return redirect(url_for("index"))


if __name__ == "__main__":
    app.run(debug=True, port=5050)