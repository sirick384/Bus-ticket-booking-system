import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class BusTicketApp {

    // ─── Color Palette ───────────────────────────────────────────────────────
    static final Color BG_DARK      = new Color(0x0F1923);
    static final Color BG_CARD      = new Color(0x1A2535);
    static final Color BG_CARD2     = new Color(0x1F2D40);
    static final Color ACCENT_RED   = new Color(0xE8394D);
    static final Color ACCENT_ORG   = new Color(0xFF6B35);
    static final Color ACCENT_GOLD  = new Color(0xFFB347);
    static final Color TEXT_WHITE   = new Color(0xF0F4F8);
    static final Color TEXT_GRAY    = new Color(0x8A9BB0);
    static final Color TEXT_LIGHT   = new Color(0xC8D6E5);
    static final Color SUCCESS      = new Color(0x2ECC71);
    static final Color BORDER_COLOR = new Color(0x2A3F55);

    static String loggedInUser = null;
    static JFrame mainFrame;

    // ─── Mock Data ────────────────────────────────────────────────────────────
    static final Map<String, String> USERS = new HashMap<>();
    static final List<Bus> BUSES = new ArrayList<>();

    static {
        USERS.put("rahul", "pass123");
        USERS.put("priya", "abc456");
        USERS.put("admin", "admin");
        USERS.put("guest", "guest");

        String[][] busData = {
            {"KA-01 Express","Bengaluru","Chennai","07:00","13:30","6h 30m","KSRTC Volvo","₹650","40","AC Sleeper"},
            {"KA-02 Night Rider","Bengaluru","Mumbai","21:00","07:30","10h 30m","VRL Travels","₹1200","45","AC Semi-Sleeper"},
            {"KA-03 Udupi Exp","Bengaluru","Mangaluru","06:30","13:00","6h 30m","NEKRTC","₹420","40","Non-AC"},
            {"KA-04 Coorg Queen","Bengaluru","Madikeri","08:00","13:30","5h 30m","SRS Travels","₹350","36","AC Seater"},
            {"KA-05 Hubli Fast","Bengaluru","Hubballi","20:00","04:00","8h","KSRTC Airavat","₹550","45","AC Sleeper"},
            {"KA-06 Mysore Exp","Bengaluru","Mysuru","06:00","09:00","3h","KSRTC","₹180","52","Non-AC"},
            {"KA-07 Goa Rider","Bengaluru","Goa","20:30","07:30","11h","Orange Tours","₹900","40","AC Sleeper"},
            {"KA-08 Ooty Breeze","Bengaluru","Ooty","06:00","12:30","6h 30m","Tamil Nadu ST","₹380","40","Non-AC"},
            {"KA-09 Pune Link","Bengaluru","Pune","18:00","06:30","12h 30m","SRS Travels","₹1100","44","AC Semi-Sleeper"},
            {"KA-10 Hydra Exp","Bengaluru","Hyderabad","21:30","06:00","8h 30m","Praveen Travels","₹750","40","AC Sleeper"},
            {"MH-11 City Fast","Mumbai","Pune","07:00","10:30","3h 30m","Neeta Tours","₹280","52","AC Seater"},
            {"MH-12 Kolhapur Exp","Mumbai","Kolhapur","22:00","06:00","8h","MSRTC","₹450","45","Non-AC"},
            {"TN-01 Kovai Exp","Chennai","Coimbatore","07:30","13:30","6h","SETC","₹380","52","Non-AC"},
            {"TN-02 Madurai Rider","Chennai","Madurai","22:00","05:00","7h","TNSTC","₹420","45","AC Seater"},
            {"KL-01 Kochi Exp","Bengaluru","Kochi","20:00","06:30","10h 30m","KSRTC Gold","₹980","40","AC Sleeper"},
        };

        Random rng = new Random(42);
        for (String[] d : busData) {
            int total = 36 + rng.nextInt(20);
            int booked = rng.nextInt(total - 5);
            BUSES.add(new Bus(d[0],d[1],d[2],d[3],d[4],d[5],d[6],d[7],total,total-booked,d[9]));
        }
    }

    // ─── Data Model ───────────────────────────────────────────────────────────
    static class Bus {
        String id, source, destination, departure, arrival, duration, operator, price;
        int totalSeats, availableSeats;
        String busType;

        Bus(String id, String src, String dst, String dep, String arr, String dur,
            String op, String price, int total, int avail, String type) {
            this.id=id; this.source=src; this.destination=dst;
            this.departure=dep; this.arrival=arr; this.duration=dur;
            this.operator=op; this.price=price;
            this.totalSeats=total; this.availableSeats=avail; this.busType=type;
        }
    }

    // ─── Main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("RideBus — Smart Bus Booking");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1100, 700);
            mainFrame.setMinimumSize(new Dimension(900, 600));
            mainFrame.setLocationRelativeTo(null);
            showLoginScreen();
            mainFrame.setVisible(true);
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOGIN SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    static void showLoginScreen() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // gradient background
                GradientPaint gp = new GradientPaint(0,0,BG_DARK,getWidth(),getHeight(),new Color(0x0A1220));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                // decorative circles
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.04f));
                g2.setColor(ACCENT_RED);
                g2.fillOval(-100,-100,400,400);
                g2.fillOval(getWidth()-200, getHeight()-200, 500, 500);
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // Left panel — branding
        JPanel leftPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, new Color(0xE8394D), 0, getHeight(), new Color(0xA01020));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                // bus silhouette as dots/pattern
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.setColor(Color.WHITE);
                for (int i = 0; i < getWidth(); i += 30)
                    for (int j = 0; j < getHeight(); j += 30)
                        g2.fillOval(i, j, 4, 4);
                g2.dispose();
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(380, 0));

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);

        JLabel busIcon = new JLabel("🚌");
        busIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        busIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brand = new JLabel("RideBus");
        brand.setFont(new Font("Georgia", Font.BOLD, 48));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Your journey, our priority");
        tagline.setFont(new Font("Georgia", Font.ITALIC, 16));
        tagline.setForeground(new Color(255,255,255,180));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 2));
        sep.setForeground(new Color(255,255,255,80));

        String[] perks = {"✓  500+ Routes Across India", "✓  Live Seat Availability", "✓  Instant E-Ticket", "✓  24×7 Support"};
        brandBox.add(busIcon);
        brandBox.add(Box.createVerticalStrut(12));
        brandBox.add(brand);
        brandBox.add(Box.createVerticalStrut(6));
        brandBox.add(tagline);
        brandBox.add(Box.createVerticalStrut(24));
        brandBox.add(sep);
        brandBox.add(Box.createVerticalStrut(20));
        for (String p : perks) {
            JLabel lbl = new JLabel(p);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setForeground(new Color(255,255,255,210));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            brandBox.add(lbl);
            brandBox.add(Box.createVerticalStrut(8));
        }
        leftPanel.add(brandBox);

        // Right panel — login form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(BG_DARK);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(380, 460));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Georgia", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to book your next journey");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField userField = styledField("Username");
        JPasswordField passField = styledPassField("Password");

        JButton loginBtn = redButton("Sign In →");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(300, 44));

        JLabel hint = new JLabel("Try: rahul / pass123");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(TEXT_GRAY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errLabel.setForeground(ACCENT_RED);
        errLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim().toLowerCase();
            String p = new String(passField.getPassword());
            if (USERS.containsKey(u) && USERS.get(u).equals(p)) {
                loggedInUser = u;
                showHomeScreen();
            } else {
                errLabel.setText("✗ Invalid credentials. Please try again.");
                passField.setText("");
            }
        });

        ActionListener enter = e -> loginBtn.doClick();
        userField.addActionListener(enter);
        passField.addActionListener(enter);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));
        card.add(fieldLabel("USERNAME"));
        card.add(Box.createVerticalStrut(6));
        card.add(userField);
        card.add(Box.createVerticalStrut(16));
        card.add(fieldLabel("PASSWORD"));
        card.add(Box.createVerticalStrut(6));
        card.add(passField);
        card.add(Box.createVerticalStrut(8));
        card.add(errLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(16));
        card.add(hint);

        rightPanel.add(card);

        root.add(leftPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        mainFrame.setContentPane(root);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HOME / SEARCH SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    static void showHomeScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);

        // ── Top Nav ──
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(BG_CARD);
        nav.setBorder(new CompoundBorder(
            new MatteBorder(0,0,1,0, BORDER_COLOR),
            new EmptyBorder(12,24,12,24)
        ));

        JLabel logo = new JLabel("🚌  RideBus");
        logo.setFont(new Font("Georgia", Font.BOLD, 22));
        logo.setForeground(ACCENT_RED);

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        navRight.setOpaque(false);

        JLabel userLbl = new JLabel("👤  " + loggedInUser.toUpperCase());
        userLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLbl.setForeground(TEXT_LIGHT);

        JButton logoutBtn = ghostButton("Logout");
        logoutBtn.addActionListener(e -> { loggedInUser=null; showLoginScreen(); });

        navRight.add(userLbl);
        navRight.add(logoutBtn);
        nav.add(logo, BorderLayout.WEST);
        nav.add(navRight, BorderLayout.EAST);

        // ── Hero Search Panel ──
        JPanel hero = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,new Color(0x0F1923),getWidth(),getHeight(),new Color(0x1A2535));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                // road lanes decoration
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.03f));
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(40));
                g2.drawLine(0, getHeight()/2+40, getWidth(), getHeight()/2+40);
                g2.dispose();
            }
        };
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(new EmptyBorder(32, 60, 32, 60));

        JLabel heroTitle = new JLabel("Search Buses");
        heroTitle.setFont(new Font("Georgia", Font.BOLD, 32));
        heroTitle.setForeground(TEXT_WHITE);
        heroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heroSub = new JLabel("Find the best buses for your journey across India");
        heroSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        heroSub.setForeground(TEXT_GRAY);
        heroSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Search card
        JPanel searchCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
        };
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 16));
        searchCard.setOpaque(false);
        searchCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        searchCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Cities
        Set<String> cities = new LinkedHashSet<>();
        for (Bus b : BUSES) { cities.add(b.source); cities.add(b.destination); }
        String[] cityArr = cities.toArray(new String[0]);
        Arrays.sort(cityArr);

        JLabel fromLbl = labelTiny("FROM");
        JComboBox<String> fromBox = styledCombo(cityArr);
        fromBox.setSelectedItem("Bengaluru");

        JLabel swapLbl = new JLabel("⇄");
        swapLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        swapLbl.setForeground(ACCENT_RED);
        swapLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel toLbl = labelTiny("TO");
        JComboBox<String> toBox = styledCombo(cityArr);
        toBox.setSelectedItem("Chennai");

        JLabel dateLbl = labelTiny("DATE");
        String[] dates = new String[7];
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, EEE");
        Calendar cal = Calendar.getInstance();
        for (int i=0;i<7;i++) { dates[i]=sdf.format(cal.getTime()); cal.add(Calendar.DATE,1); }
        JComboBox<String> dateBox = styledCombo(dates);

        JButton searchBtn = redButton("  🔍  Search Buses  ");

        swapLbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Object tmp = fromBox.getSelectedItem();
                fromBox.setSelectedItem(toBox.getSelectedItem());
                toBox.setSelectedItem(tmp);
            }
        });

        JPanel fromGroup = comboGroup(fromLbl, fromBox);
        JPanel toGroup   = comboGroup(toLbl, toBox);
        JPanel dateGroup = comboGroup(dateLbl, dateBox);

        searchCard.add(fromGroup);
        searchCard.add(makeVSep());
        searchCard.add(swapLbl);
        searchCard.add(makeVSep());
        searchCard.add(toGroup);
        searchCard.add(makeVSep());
        searchCard.add(dateGroup);
        searchCard.add(Box.createHorizontalStrut(8));
        searchCard.add(searchBtn);

        // Results area
        JPanel resultsArea = new JPanel(new BorderLayout());
        resultsArea.setBackground(BG_DARK);

        JLabel resultsTitle = new JLabel("  Popular Routes");
        resultsTitle.setFont(new Font("Georgia", Font.BOLD, 18));
        resultsTitle.setForeground(TEXT_LIGHT);
        resultsTitle.setBorder(new EmptyBorder(16,48,8,48));

        JScrollPane scrollPane = buildBusTable(BUSES, resultsArea);
        scrollPane.setBorder(new EmptyBorder(0,48,24,48));

        searchBtn.addActionListener(e -> {
            String from = (String) fromBox.getSelectedItem();
            String to   = (String) toBox.getSelectedItem();
            String date = (String) dateBox.getSelectedItem();
            if (from != null && from.equals(to)) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Source and destination cannot be the same!", "Invalid Route",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<Bus> filtered = new ArrayList<>();
            for (Bus b : BUSES)
                if ((from==null||from.isEmpty()||b.source.equalsIgnoreCase(from)) &&
                    (to==null||to.isEmpty()||b.destination.equalsIgnoreCase(to)))
                    filtered.add(b);

            resultsTitle.setText("  " + filtered.size() + " bus(es) found: " + from + " → " + to + "  |  " + date);
            JScrollPane sp = buildBusTable(filtered, resultsArea);
            sp.setBorder(new EmptyBorder(0,48,24,48));
            resultsArea.removeAll();
            resultsArea.add(resultsTitle, BorderLayout.NORTH);
            resultsArea.add(sp, BorderLayout.CENTER);
            resultsArea.revalidate(); resultsArea.repaint();
        });

        hero.add(heroTitle);
        hero.add(Box.createVerticalStrut(4));
        hero.add(heroSub);
        hero.add(Box.createVerticalStrut(20));
        hero.add(searchCard);

        resultsArea.add(resultsTitle, BorderLayout.NORTH);
        resultsArea.add(scrollPane, BorderLayout.CENTER);

        root.add(nav, BorderLayout.NORTH);
        root.add(hero, BorderLayout.CENTER);
        root.add(resultsArea, BorderLayout.SOUTH);

        // Use split to allow scroll
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.add(nav, BorderLayout.NORTH);
        topArea.add(hero, BorderLayout.CENTER);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(BG_DARK);
        mainArea.add(topArea, BorderLayout.NORTH);
        mainArea.add(resultsArea, BorderLayout.CENTER);

        JScrollPane mainScroll = new JScrollPane(mainArea);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(mainScroll);

        mainFrame.setContentPane(mainScroll);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BUS TABLE
    // ══════════════════════════════════════════════════════════════════════════
    static JScrollPane buildBusTable(List<Bus> buses, JPanel parent) {
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG_DARK);

        if (buses.isEmpty()) {
            JLabel none = new JLabel("No buses found for this route.");
            none.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            none.setForeground(TEXT_GRAY);
            none.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(30));
            listPanel.add(none);
        }

        for (Bus bus : buses) {
            JPanel row = buildBusCard(bus);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            listPanel.add(row);
            listPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane sp = new JScrollPane(listPanel);
        sp.setBackground(BG_DARK);
        sp.getViewport().setBackground(BG_DARK);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(0, 320));
        styleScrollBar(sp);
        return sp;
    }

    static JPanel buildBusCard(Bus bus) {
        JPanel card = new JPanel() {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? BG_CARD2 : BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                if (hover) {
                    g2.setColor(ACCENT_RED);
                    g2.setStroke(new BasicStroke(1.5f));
                } else {
                    g2.setColor(BORDER_COLOR);
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                // left accent bar
                g2.setColor(ACCENT_RED);
                g2.fillRoundRect(0,12,4,getHeight()-24,4,4);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14,20,14,20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left: operator + type
        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.setOpaque(false);
        leftInfo.setPreferredSize(new Dimension(190, 0));

        JLabel opName = new JLabel(bus.operator);
        opName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        opName.setForeground(TEXT_WHITE);

        JLabel busId = new JLabel(bus.id);
        busId.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        busId.setForeground(TEXT_GRAY);

        JLabel typeBadge = badge(bus.busType);

        leftInfo.add(opName);
        leftInfo.add(Box.createVerticalStrut(3));
        leftInfo.add(busId);
        leftInfo.add(Box.createVerticalStrut(6));
        leftInfo.add(typeBadge);

        // Center: route timing
        JPanel centerInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        centerInfo.setOpaque(false);

        JLabel depTime = timeLabel(bus.departure);
        JLabel srcCity = cityLabel(bus.source);
        JPanel depGroup = new JPanel(); depGroup.setLayout(new BoxLayout(depGroup, BoxLayout.Y_AXIS)); depGroup.setOpaque(false);
        depGroup.add(depTime); depGroup.add(srcCity);

        JPanel midGroup = new JPanel(); midGroup.setLayout(new BoxLayout(midGroup, BoxLayout.Y_AXIS)); midGroup.setOpaque(false);
        JLabel durLbl = new JLabel(bus.duration);
        durLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11)); durLbl.setForeground(TEXT_GRAY);
        durLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel arrow = new JLabel("————————→");
        arrow.setFont(new Font("Segoe UI", Font.PLAIN, 12)); arrow.setForeground(ACCENT_RED);
        arrow.setAlignmentX(Component.CENTER_ALIGNMENT);
        midGroup.add(durLbl); midGroup.add(arrow);

        JLabel arrTime = timeLabel(bus.arrival);
        JLabel dstCity = cityLabel(bus.destination);
        JPanel arrGroup = new JPanel(); arrGroup.setLayout(new BoxLayout(arrGroup, BoxLayout.Y_AXIS)); arrGroup.setOpaque(false);
        arrGroup.add(arrTime); arrGroup.add(dstCity);

        centerInfo.add(depGroup);
        centerInfo.add(midGroup);
        centerInfo.add(arrGroup);

        // Right: price + seats + book btn
        JPanel rightInfo = new JPanel();
        rightInfo.setLayout(new BoxLayout(rightInfo, BoxLayout.Y_AXIS));
        rightInfo.setOpaque(false);
        rightInfo.setPreferredSize(new Dimension(160, 0));
        rightInfo.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel priceLbl = new JLabel(bus.price);
        priceLbl.setFont(new Font("Georgia", Font.BOLD, 22));
        priceLbl.setForeground(ACCENT_GOLD);
        priceLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        Color seatColor = bus.availableSeats < 8 ? ACCENT_RED : SUCCESS;
        JLabel seatsLbl = new JLabel(bus.availableSeats + " seats left");
        seatsLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        seatsLbl.setForeground(seatColor);
        seatsLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JButton bookBtn = bus.availableSeats > 0 ? redButton("Book Now") : grayButton("Sold Out");
        bookBtn.setAlignmentX(Component.RIGHT_ALIGNMENT);

        if (bus.availableSeats > 0) {
            bookBtn.addActionListener(e -> showBookingScreen(bus));
        }

        rightInfo.add(priceLbl);
        rightInfo.add(Box.createVerticalStrut(2));
        rightInfo.add(seatsLbl);
        rightInfo.add(Box.createVerticalStrut(8));
        rightInfo.add(bookBtn);

        card.add(leftInfo, BorderLayout.WEST);
        card.add(centerInfo, BorderLayout.CENTER);
        card.add(rightInfo, BorderLayout.EAST);
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BOOKING SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    static void showBookingScreen(Bus bus) {
        JDialog dialog = new JDialog(mainFrame, "Book Ticket — " + bus.operator, true);
        dialog.setSize(600, 580);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setBackground(BG_DARK);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Header
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,ACCENT_RED,getWidth(),0,new Color(0xA01020));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(20, 24, 20, 24));
        header.setLayout(new BorderLayout());

        JLabel htitle = new JLabel(bus.source + "  →  " + bus.destination);
        htitle.setFont(new Font("Georgia", Font.BOLD, 20));
        htitle.setForeground(Color.WHITE);

        JLabel hsub = new JLabel(bus.operator + "  |  " + bus.departure + " – " + bus.arrival + "  |  " + bus.busType);
        hsub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hsub.setForeground(new Color(255,255,255,200));

        header.add(htitle, BorderLayout.CENTER);
        header.add(hsub, BorderLayout.SOUTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_DARK);
        form.setBorder(new EmptyBorder(24, 32, 24, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 6, 6, 6);

        JTextField nameField   = styledField("Full Name");
        JTextField ageField    = styledField("Age");
        JComboBox<String> genderBox = styledCombo(new String[]{"Male","Female","Other"});
        JTextField mobileField = styledField("Mobile Number");
        JTextField emailField  = styledField("Email Address");

        // Seat selector
        int maxSeats = Math.min(bus.availableSeats, 6);
        String[] seatOpts = new String[maxSeats];
        for (int i=0;i<maxSeats;i++) seatOpts[i]=(i+1)+" Seat"+(i>0?"s":"");
        JComboBox<String> seatsBox = styledCombo(seatOpts);

        addFormRow(form, gc, 0, "Passenger Name", nameField);
        addFormRow(form, gc, 1, "Age", ageField);
        addFormRow(form, gc, 2, "Gender", genderBox);
        addFormRow(form, gc, 3, "Mobile", mobileField);
        addFormRow(form, gc, 4, "Email", emailField);
        addFormRow(form, gc, 5, "Seats", seatsBox);

        // Price summary
        JPanel summary = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
            }
        };
        summary.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        summary.setOpaque(false);

        int basePrice = Integer.parseInt(bus.price.replace("₹","").trim());
        JLabel totalLbl = new JLabel("Total: ₹" + basePrice);
        totalLbl.setFont(new Font("Georgia", Font.BOLD, 18));
        totalLbl.setForeground(ACCENT_GOLD);
        summary.add(totalLbl);

        seatsBox.addActionListener(e -> {
            int n = seatsBox.getSelectedIndex() + 1;
            totalLbl.setText("Total: ₹" + (basePrice * n));
        });

        gc.gridx=0; gc.gridy=6; gc.gridwidth=2;
        form.add(summary, gc);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(BG_CARD);
        footer.setBorder(new MatteBorder(1,0,0,0,BORDER_COLOR));

        JButton cancelBtn = ghostButton("Cancel");
        JButton confirmBtn = redButton("  Confirm Booking  ");

        cancelBtn.addActionListener(e -> dialog.dispose());
        confirmBtn.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty() || mobileField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in Name and Mobile Number.", "Missing Info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            bus.availableSeats -= seatsBox.getSelectedIndex() + 1;
            dialog.dispose();
            showConfirmation(bus, nameField.getText().trim(),
                mobileField.getText().trim(), seatsBox.getSelectedIndex()+1,
                totalLbl.getText().replace("Total: ",""));
        });

        footer.add(cancelBtn);
        footer.add(confirmBtn);

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setBackground(BG_DARK);
        formScroll.getViewport().setBackground(BG_DARK);

        root.add(header, BorderLayout.NORTH);
        root.add(formScroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CONFIRMATION SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    static void showConfirmation(Bus bus, String name, String mobile, int seats, String total) {
        JDialog dlg = new JDialog(mainFrame, "Booking Confirmed!", true);
        dlg.setSize(460, 440);
        dlg.setLocationRelativeTo(mainFrame);

        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0,0,BG_DARK,0,getHeight(),new Color(0x0A1220));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(32, 40, 32, 40));

        JLabel checkIcon = new JLabel("✅");
        checkIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        checkIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel ctitle = new JLabel("Booking Confirmed!");
        ctitle.setFont(new Font("Georgia", Font.BOLD, 26));
        ctitle.setForeground(SUCCESS);
        ctitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        String pnr = "PNR" + (100000 + new Random().nextInt(899999));
        JLabel pnrLbl = new JLabel("PNR: " + pnr);
        pnrLbl.setFont(new Font("Courier New", Font.BOLD, 18));
        pnrLbl.setForeground(ACCENT_GOLD);
        pnrLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel ticketCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(0x2ECC71, false));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
            }
        };
        ticketCard.setLayout(new GridLayout(0,2,8,8));
        ticketCard.setOpaque(false);
        ticketCard.setBorder(new EmptyBorder(16,20,16,20));
        ticketCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticketCard.setMaximumSize(new Dimension(380, 200));

        addTicketRow(ticketCard, "Passenger", name);
        addTicketRow(ticketCard, "Mobile", mobile);
        addTicketRow(ticketCard, "Route", bus.source + " → " + bus.destination);
        addTicketRow(ticketCard, "Bus", bus.operator);
        addTicketRow(ticketCard, "Departure", bus.departure + " | " + new SimpleDateFormat("dd MMM").format(new Date()));
        addTicketRow(ticketCard, "Seats", seats + " seat(s)");
        addTicketRow(ticketCard, "Amount Paid", total);

        JButton closeBtn = redButton("  Done  ");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> dlg.dispose());

        root.add(checkIcon);
        root.add(Box.createVerticalStrut(8));
        root.add(ctitle);
        root.add(Box.createVerticalStrut(4));
        root.add(pnrLbl);
        root.add(Box.createVerticalStrut(16));
        root.add(ticketCard);
        root.add(Box.createVerticalStrut(20));
        root.add(closeBtn);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════════════════════════
    static JTextField styledField(String hint) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_CARD2); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(BORDER_COLOR); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        f.setOpaque(false); f.setForeground(TEXT_WHITE);
        f.setCaretColor(TEXT_WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(new EmptyBorder(8,12,8,12));
        f.setPreferredSize(new Dimension(240, 38));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return f;
    }

    static JPasswordField styledPassField(String hint) {
        JPasswordField f = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_CARD2); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(BORDER_COLOR); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        f.setOpaque(false); f.setForeground(TEXT_WHITE); f.setCaretColor(TEXT_WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(new EmptyBorder(8,12,8,12));
        f.setPreferredSize(new Dimension(240, 38));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return f;
    }

    static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(BG_CARD2); cb.setForeground(TEXT_WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        cb.setPreferredSize(new Dimension(160, 36));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l,v,i,sel,foc);
                setBackground(sel ? ACCENT_RED : BG_CARD2);
                setForeground(TEXT_WHITE);
                setBorder(new EmptyBorder(4,10,4,10));
                return this;
            }
        });
        return cb;
    }

    static JButton redButton(String text) {
        JButton btn = new JButton(text) {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e){hover=true;repaint();}
                public void mouseExited(MouseEvent e){hover=false;repaint();}
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = hover ? new Color(0xFF4560) : ACCENT_RED;
                Color c2 = hover ? new Color(0xC02030) : new Color(0xA01020);
                GradientPaint gp = new GradientPaint(0,0,c1,0,getHeight(),c2);
                g2.setPaint(gp); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(8,18,8,18));
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JButton ghostButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT_GRAY);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6,14,6,14)));
        btn.setBackground(BG_CARD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    static JButton grayButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(TEXT_GRAY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(8,16,8,16));
        btn.setBackground(new Color(0x2A3F55));
        btn.setFocusPainted(false);
        btn.setEnabled(false);
        return btn;
    }

    static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_GRAY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    static JLabel labelTiny(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_GRAY);
        return l;
    }

    static JLabel timeLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Georgia", Font.BOLD, 18));
        l.setForeground(TEXT_WHITE);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    static JLabel cityLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_GRAY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    static JLabel badge(String text) {
        JLabel l = new JLabel("  " + text + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean ac = getText().contains("AC");
                g2.setColor(ac ? new Color(0x1A3A5C) : new Color(0x2A1A0A));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.dispose(); super.paintComponent(g);
            }
        };
        boolean ac = text.contains("AC");
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(ac ? new Color(0x64B5F6) : ACCENT_GOLD);
        l.setOpaque(false);
        return l;
    }

    static JPanel comboGroup(JLabel lbl, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        p.add(comp);
        return p;
    }

    static JSeparator makeVSep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 40));
        s.setForeground(BORDER_COLOR);
        return s;
    }

    static void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx=0; gc.gridy=row; gc.gridwidth=1; gc.weightx=0.3;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_GRAY);
        form.add(l, gc);
        gc.gridx=1; gc.weightx=0.7;
        if (field instanceof JTextField) ((JTextField)field).setPreferredSize(new Dimension(200, 36));
        form.add(field, gc);
    }

    static void addTicketRow(JPanel panel, String key, String value) {
        JLabel k = new JLabel(key);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        k.setForeground(TEXT_GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(TEXT_WHITE);
        panel.add(k); panel.add(v);
    }

    static void styleScrollBar(JScrollPane sp) {
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(0x2A3F55);
                this.trackColor = BG_DARK;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            JButton zeroBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
    }
}
