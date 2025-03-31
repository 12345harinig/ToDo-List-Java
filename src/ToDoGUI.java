package src;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ToDoGUI {
    private JFrame frame;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JTextField taskField, dueDateField;
    private JComboBox<String> priorityBox, categoryBox;
    private List<Task> tasks;
    private boolean darkMode = false;

    private static final String FILE_NAME = "tasks.txt";
    private static final String CSV_FILE = "tasks.csv";

    public ToDoGUI() {
        tasks = new ArrayList<>();
        initializeGUI();
        loadTasksFromFile();
    }

    private void initializeGUI() {
        frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450);
        frame.setLayout(new BorderLayout());

        // Table Model
        String[] columns = {"Task", "Priority", "Due Date", "Category", "Completed"};
        tableModel = new DefaultTableModel(columns, 0);
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(taskTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        taskField = new JTextField();
        dueDateField = new JTextField();
        priorityBox = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        categoryBox = new JComboBox<>(new String[]{"Work", "Personal", "Shopping", "Others"});

        inputPanel.add(new JLabel("Task:"));
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(taskField);
        inputPanel.add(priorityBox);
        inputPanel.add(dueDateField);
        inputPanel.add(categoryBox);

        frame.add(inputPanel, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Task");
        JButton completeButton = new JButton("Mark as Done");
        JButton deleteButton = new JButton("Delete Task");
        JButton editButton = new JButton("Edit Task");
        JButton exportButton = new JButton("Export CSV");
        JButton toggleThemeButton = new JButton("Toggle Theme");

        buttonPanel.add(addButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(toggleThemeButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        addButton.addActionListener(e -> addTask());
        completeButton.addActionListener(e -> markTaskAsDone());
        deleteButton.addActionListener(e -> deleteTask());
        editButton.addActionListener(e -> editTask());
        exportButton.addActionListener(e -> exportTasksToCSV());
        toggleThemeButton.addActionListener(e -> toggleTheme());

        // Keyboard Shortcut: Press Enter to Add Task
        taskField.addActionListener(e -> addTask());

        frame.setVisible(true);
    }

    private void addTask() {
        String taskText = taskField.getText().trim();
        String priority = priorityBox.getSelectedItem().toString();
        String dueDate = dueDateField.getText().trim();
        String category = categoryBox.getSelectedItem().toString();

        if (taskText.isEmpty() || dueDate.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please fill all fields!");
            return;
        }

        // Add task to the list
        Task task = new Task(taskText, priority, dueDate, category);
        tasks.add(task);

        // Update table
        tableModel.addRow(new Object[]{taskText, priority, dueDate, category, "No"});

        // Save to file
        saveTasksToFile();

        // Clear input fields
        taskField.setText("");
        dueDateField.setText("");
    }

    private void markTaskAsDone() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a task to mark as done!");
            return;
        }

        // Update task status
        tasks.get(selectedRow).setCompleted(true);

        // Update table
        tableModel.setValueAt("Yes", selectedRow, 4);

        // Save to file
        saveTasksToFile();
    }

    private void deleteTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a task to delete!");
            return;
        }

        // Remove from list and table
        tasks.remove(selectedRow);
        tableModel.removeRow(selectedRow);

        // Save to file
        saveTasksToFile();
    }

    private void editTask() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Select a task to edit!");
            return;
        }

        // Get current values
        String newTask = JOptionPane.showInputDialog("Edit Task:", tasks.get(selectedRow).getDescription());
        if (newTask != null && !newTask.trim().isEmpty()) {
            tasks.get(selectedRow).setDescription(newTask);
            tableModel.setValueAt(newTask, selectedRow, 0);
            saveTasksToFile();
        }
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        frame.getContentPane().setBackground(darkMode ? Color.DARK_GRAY : Color.WHITE);
        taskTable.setBackground(darkMode ? Color.LIGHT_GRAY : Color.WHITE);
    }

    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task task : tasks) {
                writer.write(task.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksFromFile() {
        if (!Files.exists(Paths.get(FILE_NAME))) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                Task task = new Task(parts[0], parts[1], parts[2], parts[3]);
                task.setCompleted(Boolean.parseBoolean(parts[4]));
                tasks.add(task);
                tableModel.addRow(new Object[]{parts[0], parts[1], parts[2], parts[3], parts[4].equals("true") ? "Yes" : "No"});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportTasksToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
            writer.write("Task,Priority,Due Date,Category,Completed\n");
            for (Task task : tasks) {
                writer.write(task.toFileString());
                writer.newLine();
            }
            JOptionPane.showMessageDialog(frame, "Tasks exported to " + CSV_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoGUI::new);
    }
}

class Task {
    private String description, priority, dueDate, category;
    private boolean completed;

    public Task(String description, String priority, String dueDate, String category) {
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.category = category;
        this.completed = false;
    }

    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return description; }
    public String toFileString() { return description + "," + priority + "," + dueDate + "," + category + "," + completed; }
}
