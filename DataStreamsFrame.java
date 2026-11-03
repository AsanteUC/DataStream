import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Data Streams window. The user loads a text file with a JFileChooser, types
 * a search string, and the lines of the file that contain the string are found
 * with a Java Stream and shown next to the original file.
 *
 * @author Asante
 * @version 1.0
 */
public class DataStreamsFrame extends JFrame
{
    private Path loadedPath = null;

    private JLabel fileLabel;
    private JLabel resultLabel;
    private JTextField searchField;
    private JTextArea originalArea;
    private JTextArea filteredArea;
    private JButton searchButton;

    private final Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
    private final Font textFont = new Font("Monospaced", Font.PLAIN, 13);

    /**
     * Builds the window.
     */
    public DataStreamsFrame()
    {
        super("Data Streams: Search a File");
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(createTextPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private JPanel createTopPanel()
    {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        fileLabel = new JLabel("File: (none loaded)");
        fileLabel.setFont(labelFont);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        JLabel searchLabel = new JLabel("Search string:");
        searchLabel.setFont(labelFont);
        searchField = new JTextField();
        searchField.setFont(labelFont);
        searchField.addActionListener(ae -> searchFile()); // Enter also searches
        searchRow.add(searchLabel, BorderLayout.WEST);
        searchRow.add(searchField, BorderLayout.CENTER);

        panel.add(fileLabel);
        panel.add(searchRow);
        return panel;
    }

    private JPanel createTextPanel()
    {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        originalArea = createTextArea();
        filteredArea = createTextArea();

        panel.add(createScrollPane(originalArea, "Original File"));
        panel.add(createScrollPane(filteredArea, "Filtered Lines"));
        return panel;
    }

    private JTextArea createTextArea()
    {
        JTextArea area = new JTextArea();
        area.setFont(textFont);
        area.setEditable(false);
        return area;
    }

    private JScrollPane createScrollPane(JTextArea area, String title)
    {
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    private JPanel createButtonPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());

        resultLabel = new JLabel(" ");
        resultLabel.setFont(labelFont);
        resultLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton loadButton = new JButton("Load File");
        loadButton.addActionListener(ae -> loadFile());

        searchButton = new JButton("Search");
        searchButton.setEnabled(false); // only after a file is loaded
        searchButton.addActionListener(ae -> searchFile());

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(ae -> quit());

        buttons.add(loadButton);
        buttons.add(searchButton);
        buttons.add(quitButton);

        panel.add(resultLabel, BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Lets the user pick a text file and shows it in the left text area.
     */
    private void loadFile()
    {
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle("Choose a text file");
        chooser.setFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();

        // Read the whole file as a stream of lines and join them for display
        try (Stream<String> lines = Files.lines(path))
        {
            String text = lines.collect(Collectors.joining("\n"));

            loadedPath = path;
            originalArea.setText(text);
            originalArea.setCaretPosition(0);
            filteredArea.setText("");
            fileLabel.setText("File: " + path.getFileName());
            resultLabel.setText(" ");
            searchButton.setEnabled(true);
        }
        catch (IOException | UncheckedIOException e)
        {
            showError("Could not read the file. Make sure it is a plain text file.\n" + e.getMessage());
        }
    }

    /**
     * Uses a Stream with a lambda filter to find the lines of the loaded file
     * that contain the search string (ignoring case), and shows them in the
     * right text area.
     */
    private void searchFile()
    {
        if (loadedPath == null)
        {
            showError("Please load a file first.");
            return;
        }

        String searchString = searchField.getText().trim();
        if (searchString.isEmpty())
        {
            showError("Please enter a search string.");
            return;
        }
        String target = searchString.toLowerCase();

        try (Stream<String> lines = Files.lines(loadedPath))
        {
            // The stream approach: filter the lines with a lambda expression
            List<String> matches = lines
                    .filter(line -> line.toLowerCase().contains(target))
                    .collect(Collectors.toList());

            if (matches.isEmpty())
            {
                filteredArea.setText("(No lines contain \"" + searchString + "\")");
            }
            else
            {
                filteredArea.setText(String.join("\n", matches));
            }
            filteredArea.setCaretPosition(0);
            resultLabel.setText(matches.size() + " line(s) contain \"" + searchString + "\"");
        }
        catch (IOException | UncheckedIOException e)
        {
            showError("Could not read the file.\n" + e.getMessage());
        }
    }

    private void quit()
    {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?",
                "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
    }

    private void showError(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
