# 🚆 Java Train Reservation System (Swing GUI)

This is a desktop-based Train Reservation System developed in Java using the **Swing** framework. It allows users to book tickets, cancel bookings, check availability, and manage a waiting list, with persistent data storage using `.dat` files.

---

## 📌 Features

- Book train tickets via a user-friendly GUI
- Cancel existing bookings
- Check train availability
- Display booking details and waiting list
- Persistent storage using `.dat` files (serialization)
- Auto-generated PNR using `AtomicInteger`
- Multiple tabbed UI using `JTabbedPane`

---

## 💡 Technologies Used

- Java Swing (GUI)
- Java I/O (File handling)
- Java Collections (`ArrayList`, `List`)
- Object Serialization (`.dat` files)
- Event-driven programming (`ActionListener`)
- Multithreading-safe `AtomicInteger` for PNR

---

## 📁 File Structure

- `tut29.java` – Main application file
- `trains.dat` – Serialized train data
- `bookings.dat` – Serialized booking data
- `waiting_list.dat` – Serialized waiting list data

---

## ▶️ How to Run

1. Compile the Java file:
   ```bash
   javac tut29.java
