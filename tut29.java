import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class tut29 extends JFrame {

    private static final String TRAINS_FILE = "trains.dat";
    private static final String BOOKINGS_FILE = "bookings.dat";
    private static final String WAITING_LIST_FILE = "waiting_list.dat";
    private static final AtomicInteger pnrCounter = new AtomicInteger(500);

    private List<Train> trains = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();
    private List<WaitingList> waitingList = new ArrayList<>();

    // GUI Components
    private JTabbedPane tabbedPane;
    private JPanel bookTicketPanel, cancelTicketPanel, availabilityPanel, bookingDetailsPanel, waitingListPanel;

    // Colors
    private final Color PRIMARY_COLOR = new Color(0, 102, 102);
    private final Color SECONDARY_COLOR = new Color(204, 255, 255);
    private final Color ACCENT_COLOR = new Color(255, 153, 0);

    public tut29() {
        setTitle("Railway Reservation System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            setIconImage(new ImageIcon("train.png").getImage());
        } catch (Exception e) {
            System.out.println("Icon image not found, using default icon");
        }

        // Load data
        loadData();

        // Initialize UI
        initUI();

        // Start with first tab selected
        tabbedPane.setSelectedIndex(0);
    }

    private void initUI() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(PRIMARY_COLOR);
        tabbedPane.setForeground(Color.WHITE);

        // Create panels for each tab
        createBookTicketPanel();
        createCancelTicketPanel();
        createAvailabilityPanel();
        createBookingDetailsPanel();
        createWaitingListPanel();

        // Add tabs
        tabbedPane.addTab("Book Ticket", bookTicketPanel);
        tabbedPane.addTab("Cancel Ticket", cancelTicketPanel);
        tabbedPane.addTab("Check Availability", availabilityPanel);
        tabbedPane.addTab("Booking Details", bookingDetailsPanel);
        tabbedPane.addTab("Waiting List", waitingListPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Add header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        JLabel titleLabel = new JLabel("INDIAN RAILWAY RESERVATION SYSTEM");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Add footer panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(PRIMARY_COLOR);
        JLabel footerLabel = new JLabel("© 2025 Indian Railways. All Rights Reserved.");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void createBookTicketPanel() {
        bookTicketPanel = new JPanel(new BorderLayout(10, 10));
        bookTicketPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bookTicketPanel.setBackground(SECONDARY_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        JLabel headerLabel = new JLabel("BOOK YOUR TICKET");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        bookTicketPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBackground(SECONDARY_COLOR);

        JLabel trainNoLabel = new JLabel("Train Number:");
        trainNoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JComboBox<String> trainNoCombo = new JComboBox<>();
        for (Train train : trains) {
            trainNoCombo.addItem(train.getNumber() + " - " + train.getName());
        }

        JLabel dateLabel = new JLabel("Travel Date (dd-mm-yyyy):");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField dateField = new JTextField();

        JLabel passengerLabel = new JLabel("Number of Passengers:");
        passengerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JSpinner passengerSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        JLabel namesLabel = new JLabel("Passenger Names (comma separated):");
        namesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField namesField = new JTextField();

        formPanel.add(trainNoLabel);
        formPanel.add(trainNoCombo);
        formPanel.add(dateLabel);
        formPanel.add(dateField);
        formPanel.add(passengerLabel);
        formPanel.add(passengerSpinner);
        formPanel.add(namesLabel);
        formPanel.add(namesField);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        JButton bookButton = new JButton("BOOK NOW");
        bookButton.setBackground(ACCENT_COLOR);
        bookButton.setForeground(Color.BLACK);
        bookButton.setFont(new Font("Arial", Font.BOLD, 14));
        bookButton.addActionListener(e -> {
            String selectedTrain = (String) trainNoCombo.getSelectedItem();
            String trainNumber = selectedTrain.split(" - ")[0];
            String dateStr = dateField.getText();
            int passengers = (int) passengerSpinner.getValue();
            String[] passengerNames = namesField.getText().split(",");

            if (passengerNames.length != passengers) {
                JOptionPane.showMessageDialog(this, "Number of names doesn't match passenger count!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date travelDate;
            try {
                travelDate = new SimpleDateFormat("dd-MM-yyyy").parse(dateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format! Please use dd-mm-yyyy", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Train train = findTrain(trainNumber);
            int availableSeats = train.getTotalSeats() - getBookedSeats(trainNumber, travelDate);

            if (availableSeats >= passengers) {
                int pnr = pnrCounter.getAndIncrement();
                Booking booking = new Booking(pnr, trainNumber, travelDate, passengerNames);
                bookings.add(booking);
                saveData();

                String message = "Booking successful!\n"
                        + "PNR: " + pnr + "\n"
                        + "Train: " + train.getName() + " (" + trainNumber + ")\n"
                        + "Date: " + new SimpleDateFormat("dd-MM-yyyy").format(travelDate) + "\n"
                        + "Passengers: " + String.join(", ", passengerNames);
                JOptionPane.showMessageDialog(this, message, "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);

                // Clear fields
                dateField.setText("");
                passengerSpinner.setValue(1);
                namesField.setText("");
            } else {
                int waitingNumber = waitingList.size() + 1;
                WaitingList wl = new WaitingList(waitingNumber, trainNumber, travelDate, passengerNames);
                waitingList.add(wl);
                saveData();

                String message = "No seats available! Added to waiting list.\n"
                        + "Waiting list number: " + waitingNumber + "\n"
                        + "You will be notified if seats become available.";
                JOptionPane.showMessageDialog(this, message, "Waiting List", JOptionPane.WARNING_MESSAGE);
            }
        });

        buttonPanel.add(bookButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setBackground(SECONDARY_COLOR);

        bookTicketPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void createCancelTicketPanel() {
        cancelTicketPanel = new JPanel(new BorderLayout(5, 5));
        cancelTicketPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cancelTicketPanel.setBackground(SECONDARY_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        JLabel headerLabel = new JLabel("TICKET CANCELLATION");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        cancelTicketPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(1, 2, 1, 1));
        formPanel.setBackground(SECONDARY_COLOR);

        JLabel pnrLabel = new JLabel("Enter PNR Number:");
        pnrLabel.setFont(new Font("Arial", Font.BOLD, 20));
        JTextField pnrField = new JTextField();
        pnrField.setPreferredSize(new Dimension(100,20));

        formPanel.add(pnrLabel);
        formPanel.add(pnrField);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        JButton cancelButton = new JButton("CANCEL TICKET");
        cancelButton.setBackground(Color.RED);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.addActionListener(e -> {
            try {
                int pnr = Integer.parseInt(pnrField.getText());
                Booking bookingToRemove = null;

                for (Booking booking : bookings) {
                    if (booking.getPnr() == pnr) {
                        bookingToRemove = booking;
                        break;
                    }
                }

                if (bookingToRemove != null) {
                    bookings.remove(bookingToRemove);
                    saveData();
                    JOptionPane.showMessageDialog(this, "Ticket cancelled successfully!", "Cancellation", JOptionPane.INFORMATION_MESSAGE);
                    pnrField.setText("");

                    // Check waiting list for auto-confirmation
                    autoConfirmFromWaitingList(bookingToRemove.getTrainNumber(), bookingToRemove.getTravelDate());
                } else {
                    JOptionPane.showMessageDialog(this, "Booking not found with PNR: " + pnr, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid PNR number! Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setBackground(SECONDARY_COLOR);

        cancelTicketPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void createAvailabilityPanel() {
        availabilityPanel = new JPanel(new BorderLayout(10, 10));
        availabilityPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        availabilityPanel.setBackground(SECONDARY_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        JLabel headerLabel = new JLabel("CHECK AVAILABILITY");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        availabilityPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBackground(SECONDARY_COLOR);

        JLabel trainNoLabel = new JLabel("Train Number:");
        trainNoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JComboBox<String> trainNoCombo = new JComboBox<>();
        for (Train train : trains) {
            trainNoCombo.addItem(train.getNumber() + " - " + train.getName());
        }

        JLabel dateLabel = new JLabel("Travel Date (dd-mm-yyyy):");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField dateField = new JTextField();
        dateField.setPreferredSize(new Dimension(50,10));

        formPanel.add(trainNoLabel);
        formPanel.add(trainNoCombo);
        formPanel.add(dateLabel);
        formPanel.add(dateField);

        // Result Area
        JTextArea resultArea = new JTextArea(10, 10);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        JButton checkButton = new JButton("CHECK AVAILABILITY");
        checkButton.setBackground(ACCENT_COLOR);
        checkButton.setForeground(Color.BLACK);
        checkButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkButton.addActionListener(e -> {
            String selectedTrain = (String) trainNoCombo.getSelectedItem();
            String trainNumber = selectedTrain.split(" - ")[0];
            String dateStr = dateField.getText();

            Date travelDate;
            try {
                travelDate = new SimpleDateFormat("dd-MM-yyyy").parse(dateStr);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format! Please use dd-mm-yyyy", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Train train = findTrain(trainNumber);
            int bookedSeats = getBookedSeats(trainNumber, travelDate);
            int availableSeats = train.getTotalSeats() - bookedSeats;

            String result = "Availability for " + train.getName() + " (" + trainNumber + ")\n"
                    + "Date: " + new SimpleDateFormat("dd-MM-yyyy").format(travelDate) + "\n"
                    + "Total seats: " + train.getTotalSeats() + "\n"
                    + "Booked seats: " + bookedSeats + "\n"
                    + "Available seats: " + availableSeats + "\n\n";

            if (availableSeats > 0) {
                result += "Status: AVAILABLE\nYou can book your ticket!";
            } else {
                result += "Status: FULL\nPlease check waiting list options.";
            }

            resultArea.setText(result);
        });

        buttonPanel.add(checkButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setBackground(SECONDARY_COLOR);

        availabilityPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void createBookingDetailsPanel() {
        bookingDetailsPanel = new JPanel(new BorderLayout(10, 10));
        bookingDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bookingDetailsPanel.setBackground(SECONDARY_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        JLabel headerLabel = new JLabel("BOOKING DETAILS");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        bookingDetailsPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        formPanel.setBackground(SECONDARY_COLOR);

        JLabel pnrLabel = new JLabel("Enter PNR Number:");
        pnrLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField pnrField = new JTextField();

        formPanel.add(pnrLabel);
        formPanel.add(pnrField);

        // Result Area
        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        JButton searchButton = new JButton("SEARCH");
        searchButton.setBackground(ACCENT_COLOR);
        searchButton.setForeground(Color.BLACK);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.addActionListener(e -> {
            try {
                int pnr = Integer.parseInt(pnrField.getText());
                boolean found = false;

                for (Booking booking : bookings) {
                    if (booking.getPnr() == pnr) {
                        Train train = findTrain(booking.getTrainNumber());
                        String details = "Booking Details:\n"
                                + "PNR: " + pnr + "\n"
                                + "Train: " + train.getName() + " (" + booking.getTrainNumber() + ")\n"
                                + "Date: " + new SimpleDateFormat("dd-MM-yyyy").format(booking.getTravelDate()) + "\n"
                                + "Passengers:\n";

                        for (String name : booking.getPassengerNames()) {
                            details += " - " + name + "\n";
                        }

                        resultArea.setText(details);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    resultArea.setText("Booking not found with PNR: " + pnr);
                }
            } catch (NumberFormatException ex) {
                resultArea.setText("Invalid PNR number! Please enter a valid number.");
            }
        });

        buttonPanel.add(searchButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setBackground(SECONDARY_COLOR);

        bookingDetailsPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void createWaitingListPanel() {
        waitingListPanel = new JPanel(new BorderLayout(10, 10));
        waitingListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        waitingListPanel.setBackground(SECONDARY_COLOR);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        JLabel headerLabel = new JLabel("WAITING LIST");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        waitingListPanel.add(headerPanel, BorderLayout.NORTH);

        // Table for waiting list
        String[] columnNames = {"Waiting No", "Train No", "Train Name", "Date", "Passengers"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable waitingListTable = new JTable(model);
        waitingListTable.setFont(new Font("Arial", Font.PLAIN, 12));
        waitingListTable.setRowHeight(25);
        waitingListTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        waitingListTable.getTableHeader().setBackground(PRIMARY_COLOR);
        waitingListTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(waitingListTable);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        JButton refreshButton = new JButton("REFRESH");
        refreshButton.setBackground(ACCENT_COLOR);
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));
        refreshButton.addActionListener(e -> {
            model.setRowCount(0); // Clear table

            for (WaitingList wl : waitingList) {
                Train train = findTrain(wl.getTrainNumber());
                model.addRow(new Object[]{
                    wl.getWaitingNumber(),
                    wl.getTrainNumber(),
                    train.getName(),
                    new SimpleDateFormat("dd-MM-yyyy").format(wl.getTravelDate()),
                    String.join(", ", wl.getPassengerNames())
                });
            }

            if (waitingList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No passengers in waiting list.", "Waiting List", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        buttonPanel.add(refreshButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setBackground(SECONDARY_COLOR);

        waitingListPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void autoConfirmFromWaitingList(String trainNumber, Date travelDate) {
        List<WaitingList> matchingWaitList = new ArrayList<>();
        for (WaitingList wl : waitingList) {
            if (wl.getTrainNumber().equals(trainNumber) && wl.getTravelDate().equals(travelDate)) {
                matchingWaitList.add(wl);
            }
        }

        if (!matchingWaitList.isEmpty()) {
            WaitingList firstInLine = matchingWaitList.get(0);
            int availableSeats = findTrain(trainNumber).getTotalSeats()
                    - getBookedSeats(trainNumber, travelDate);

            if (availableSeats >= firstInLine.getPassengerCount()) {
                // Move from waiting list to confirmed booking
                int pnr = pnrCounter.getAndIncrement();
                Booking booking = new Booking(pnr, firstInLine.getTrainNumber(),
                        firstInLine.getTravelDate(),
                        firstInLine.getPassengerNames());
                bookings.add(booking);
                waitingList.remove(firstInLine);
                saveData();

                String message = "Auto-confirmation for waiting list passenger:\n"
                        + "PNR: " + pnr + "\n"
                        + "Passengers: " + String.join(", ", firstInLine.getPassengerNames()) + "\n"
                        + "Your booking is now confirmed!";
                JOptionPane.showMessageDialog(this, message, "Auto Confirmation", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private Train findTrain(String trainNumber) {
        for (Train train : trains) {
            if (train.getNumber().equals(trainNumber)) {
                return train;
            }
        }
        return null;
    }

    private int getBookedSeats(String trainNumber, Date travelDate) {
        int count = 0;
        for (Booking booking : bookings) {
            if (booking.getTrainNumber().equals(trainNumber)
                    && booking.getTravelDate().equals(travelDate)) {
                count += booking.getPassengerCount();
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            // Load trains
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TRAINS_FILE));
            trains = (List<Train>) ois.readObject();
            ois.close();

            // Load bookings
            ois = new ObjectInputStream(new FileInputStream(BOOKINGS_FILE));
            bookings = (List<Booking>) ois.readObject();
            ois.close();

            // Load waiting list
            ois = new ObjectInputStream(new FileInputStream(WAITING_LIST_FILE));
            waitingList = (List<WaitingList>) ois.readObject();
            ois.close();

            // Initialize PNR counter
            if (!bookings.isEmpty()) {
                int maxPnr = bookings.stream().mapToInt(Booking::getPnr).max().getAsInt();
                pnrCounter.set(maxPnr + 1);
            }
        } catch (FileNotFoundException e) {
            // Initialize with default trains if files don't exist
            initializeDefaultTrains();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void saveData() {
        try {
            // Save trains
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TRAINS_FILE));
            oos.writeObject(trains);
            oos.close();

            // Save bookings
            oos = new ObjectOutputStream(new FileOutputStream(BOOKINGS_FILE));
            oos.writeObject(bookings);
            oos.close();

            // Save waiting list
            oos = new ObjectOutputStream(new FileOutputStream(WAITING_LIST_FILE));
            oos.writeObject(waitingList);
            oos.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeDefaultTrains() {
        trains.add(new Train("Rajdhani Express", "12301", 50));
        trains.add(new Train("Shatabdi Express", "12002", 40));
        trains.add(new Train("Duronto Express", "12245", 60));
        trains.add(new Train("Garib Rath", "12213", 70));
        trains.add(new Train("Sampark Kranti", "12446", 55));
    }

    // Inner classes for data models
    public static class Train implements Serializable {

        private final String name;
        private final String number;
        private final int totalSeats;

        public Train(String name, String number, int totalSeats) {
            this.name = name;
            this.number = number;
            this.totalSeats = totalSeats;
        }

        public String getName() {
            return name;
        }

        public String getNumber() {
            return number;
        }

        public int getTotalSeats() {
            return totalSeats;
        }
    }

    public static class Booking implements Serializable {

        private final int pnr;
        private final String trainNumber;
        private final Date travelDate;
        private final String[] passengerNames;

        public Booking(int pnr, String trainNumber, Date travelDate, String[] passengerNames) {
            this.pnr = pnr;
            this.trainNumber = trainNumber;
            this.travelDate = travelDate;
            this.passengerNames = passengerNames;
        }

        public int getPnr() {
            return pnr;
        }

        public String getTrainNumber() {
            return trainNumber;
        }

        public Date getTravelDate() {
            return travelDate;
        }

        public String[] getPassengerNames() {
            return passengerNames;
        }

        public int getPassengerCount() {
            return passengerNames.length;
        }
    }

    public static class WaitingList implements Serializable {

        private final int waitingNumber;
        private final String trainNumber;
        private final Date travelDate;
        private final String[] passengerNames;

        public WaitingList(int waitingNumber, String trainNumber, Date travelDate, String[] passengerNames) {
            this.waitingNumber = waitingNumber;
            this.trainNumber = trainNumber;
            this.travelDate = travelDate;
            this.passengerNames = passengerNames;
        }

        public int getWaitingNumber() {
            return waitingNumber;
        }

        public String getTrainNumber() {
            return trainNumber;
        }

        public Date getTravelDate() {
            return travelDate;
        }

        public String[] getPassengerNames() {
            return passengerNames;
        }

        public int getPassengerCount() {
            return passengerNames.length;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            tut29 system = new tut29();
            system.setVisible(true);

            // Add window listener to save data on close
            system.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    system.saveData();
                }
            });
        });
    }
}
