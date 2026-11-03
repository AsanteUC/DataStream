import javax.swing.SwingUtilities;

/**
 * Starts the Data Streams application.
 *
 * @author Asante
 * @version 1.0
 */
public class DataStreamsRunner
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            DataStreamsFrame frame = new DataStreamsFrame();
            frame.setVisible(true);
        });
    }
}
