import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class Theme {
    public static final Color BG_DARK   = new Color(20, 20, 30);
    public static final Color BG_MID    = new Color(30, 30, 44);
    public static final Color BG_CARD   = new Color(48, 48, 68);
    public static final Color ACCENT    = new Color(99, 102, 241);
    public static final Color TEXT_WHT  = Color.WHITE;
    public static final Color TEXT_GRAY = new Color(170, 170, 190);
    public static final Color GREEN     = new Color(74, 222, 128);
    public static final Color ORANGE    = new Color(251, 146, 60);
    public static final Color RED       = new Color(248, 113, 113);
    public static final Color YELLOW    = new Color(250, 204, 21);

    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);

    public static JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG_MID);
        return p;
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font); l.setForeground(color);
        return l;
    }

    public static JButton styledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return b;
    }

    public static JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        if (title != null) {
            JLabel t = label(title, FONT_HEADING, TEXT_WHT);
            t.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
            p.add(t, BorderLayout.NORTH);
        }
        return p;
    }

    public static JPanel topBar(String subtitle) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DARK);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = label("⬡ UNIFIED GAMING PLATFORM", FONT_HEADING, ACCENT);
        bar.add(title, BorderLayout.WEST);

        if (subtitle != null) {
            JLabel sub = label(subtitle, FONT_SMALL, TEXT_GRAY);
            bar.add(sub, BorderLayout.EAST);
        }
        return bar;
    }

    public static String now() {
        java.time.LocalTime t = java.time.LocalTime.now();
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }
}