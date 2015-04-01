package iconifier;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Controller {

    private static BufferedImage src = null;
    private static File dest = null;

    public Button beautifullyNamedButton;
    public Button destSelectButton;
    public Button imageSelectButton;
    public Button startButton;
    public Label destLabel;
    public Label messageLabel;
    public Label imageDirectoryLabel;
    public Label resolutionLabel;
    public ImageView iconPreview;

    @SuppressWarnings("unused") //Used implicitly by the FXML loader, not called directly anywhere in the program
    @FXML
    void initialize() {
        beautifullyNamedButton.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(new URL("http://www.twitter.com/DoktuhParadox").toURI());
            } catch (IOException | URISyntaxException e) {
                System.out.println("Couldn\'t open twitter link for you. Sorry :C");
            }
        });

        imageSelectButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose source image");
            fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Restrict only to png image files", "png"));
            File chosenFile = fileChooser.showOpenDialog(win());

            if (chosenFile != null && chosenFile.getName().endsWith(".png") /*JUST TO MAKE SURE!*/) {
                try {
                    Image img = new Image(chosenFile.toURI().toURL().toString());
                    int width = (int) img.getWidth(), height = (int) img.getHeight();
                    imageDirectoryLabel.setText(chosenFile.getName());

                    if (width == height) {
                        iconPreview.setImage(img);
                        resolutionLabel.setText(String.format("Resolution: %sx%s", width, height));

                        //Packs the given image into a BufferedImage
                        BufferedImage bimage = new BufferedImage((int) img.getWidth(), (int) img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D bGr = bimage.createGraphics();
                        bGr.drawImage(SwingFXUtils.fromFXImage(img, null) /*Converts from an FX image to an AWT image*/, 0, 0, null);
                        bGr.dispose();
                        src = bimage;
                    } else {
                        messageLabel.setText("Image width and height must be equal.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        destSelectButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose destination directory");
            File chosenFile = directoryChooser.showDialog(win());

            if (chosenFile != null) {
                dest = chosenFile;
                destLabel.setText(dest.getAbsolutePath());
            }
        });

        startButton.setOnAction(event -> {
            if (src == null) {
                messageLabel.setText("Select a valid source image.");
            } else if (dest == null) {
                messageLabel.setText("Select a destination directory.");
            } else {
                try {
                    File[] files = dest.listFiles();

                    if (files != null && files.length > 0) {
                        int processedImagesCount = 0, skippedImagesCount = 0;

                        for (File imgFile : files) {
                            if (imgFile.getName().endsWith(".png")) {
                                Image img = new Image(imgFile.toURI().toURL().toString());

                                if (img.getWidth() == img.getHeight()) {
                                    System.out.printf("Processing %s (resolution %sx%s)\n", imgFile.getPath(), img.getWidth(), img.getHeight());
                                    ImageIO.write(Scalr.resize(src, (int) img.getWidth()), "png", imgFile); //Only overwrites the contents of the original image file, reusing it
                                    processedImagesCount++;
                                } else {
                                    System.out.printf("Ignoring %s; invalid resolution (%sx%s); width must equal height\n", imgFile.getPath(), img.getWidth(), img.getHeight());
                                    skippedImagesCount++;
                                }
                            }
                        }

                        messageLabel.setText(String.format("Processed %s, skipped %s", processedImagesCount, skippedImagesCount));
                    } else {
                        messageLabel.setText("Destination directory does not contain any images.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Utility method. This is to acquire the
     * window object after it has been initialized.
     *
     * @return the parent window.
     */
    private Window win() {
        return imageSelectButton.getParent().getScene().getWindow();
    }
}
