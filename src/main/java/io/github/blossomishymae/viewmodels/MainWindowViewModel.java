package io.github.blossomishymae.viewmodels;

import com.google.inject.Singleton;
import io.github.blossomishymae.componentmodel.ObservableObject;
import io.github.blossomishymae.event.ISimpleEventSubscription;
import io.github.blossomishymae.event.SimpleEventHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Singleton
public class MainWindowViewModel extends ObservableObject {
    public enum Dimension {
        WIDTH,
        HEIGHT;

        public Image getScaledInstance(final ImageIcon icon, int width, int height) {
            Image image = null;
            switch(this)  {
                case WIDTH -> image = icon.getImage().getScaledInstance(width, -1, Image.SCALE_SMOOTH);
                case HEIGHT -> image = icon.getImage().getScaledInstance(-1, height, Image.SCALE_SMOOTH);
            }
            MediaTracker tracker = new MediaTracker(new Container());
            tracker.addImage(image, 0);
            try {
                tracker.waitForAll();
                return image;
            } catch (InterruptedException ex) {
                throw new RuntimeException("Failed to load image!");
            }
        }
    }
    public enum Mode {
        NORMAL,
        FILE;

        public String getStatusText() {
            String statusText = null;
            switch(this)  {
                case NORMAL -> statusText = "Normal mode";
                case FILE -> statusText = "File mode";
            }
            return statusText;
        }

        public void setClipboard(final Image image, final File file)
        throws IOException {
            switch(this) {
                case NORMAL -> setClipboard(image);
                case FILE -> {
                    String path = file.getPath();
                    String extension = path.substring(path.indexOf("."));
                    setClipboard(image, extension);
                }
            }
        }

        private void setClipboard(final Image image, final String extension)
        throws IOException {
            File tempFile = File.createTempFile("smol-image", extension);
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            bi.getGraphics().drawImage(image, 0, 0, null);
            ImageIO.write(bi, extension.toUpperCase().replace(".", ""), tempFile);
            setClipboard(tempFile);
        }

        private void setClipboard(final Image image) {
            ImageSelection imageSelection = new ImageSelection(image);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageSelection, null);
        }

        private void setClipboard(final File file) {
            List<File> files = new ArrayList<>();
            files.add(file);
            FileSelection fileSelection = new FileSelection(files);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(fileSelection, null);
        }
    }
    private record FileSelection(List<File> files) implements Transferable {

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.javaFileListFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return files;
        }
    }
    private record ImageSelection(Image image) implements Transferable {

        public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (!DataFlavor.imageFlavor.equals(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
    private static class ImageWorker extends SwingWorker<Image, Void> {
        private final Supplier<Image> supplier;
        public SimpleEventHandler<Image> finished = new SimpleEventHandler<>();

        public ImageWorker(Supplier<Image> supplier) {
            this.supplier = supplier;
        }

        @Override
        protected Image doInBackground() throws Exception {
            return supplier.get();
        }

        @Override
        protected void done() {
            try {
                Image image = get();
                finished.invoke(image);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
        }
    }
    private final Image applicationIcon;

    private int width;
    private int height;
    private Dimension dimension = Dimension.WIDTH;
    private String statusText = "";
    private Mode mode = Mode.NORMAL;

    public  MainWindowViewModel() {
        super();
        Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png"));
        applicationIcon = icon.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getStatusText() {
        return statusText;
    }

    public Dimension getDimension() { return dimension; }

    public Image getApplicationIcon() {
        return applicationIcon;
    }

    public Mode getMode() { return mode; }

    public void setWidth(final int width) {
        this.width = width;
        notifyPropertyChanged("setWidth");
    }

    public void setWidth(final String width) {
        if (!isInteger(width)) {
            setStatusText("Dimension must be a valid number!");
            return;
        }
        setWidth(Integer.parseInt(width));
        setStatusText(" ");
    }

    public void setHeight(final int height) {
        this.height = height;
        notifyPropertyChanged("setHeight");
    }

    public void setHeight(final String height) {
        if (!isInteger(height)) {
            setStatusText("Dimension must be a valid number!");
            return;
        }
        setHeight(Integer.parseInt(height));
        setStatusText(" ");
    }

    public void setStatusText(final String statusText) {
        this.statusText = statusText;
        notifyPropertyChanged("setStatusText");
    }

    public void setDimension(final Dimension dimension) {
        this.dimension = dimension;
        notifyPropertyChanged("setDimension");
    }

    public void setMode(final Mode mode) {
        this.mode = mode;
        notifyPropertyChanged("setMode");
    }

    public void toggleMode() {
        setMode(mode == Mode.FILE ? Mode.NORMAL : Mode.FILE);
    }

    public DropTarget getDropTarget() {
        return new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)evt
                            .getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    // Only get the first file dropped if any
                    for (File file : droppedFiles) {
                        ImageWorker worker = new ImageWorker(decorateScaleImage(file));
                        worker.finished.subscribe(decorateOnFinished(worker, file));
                        worker.run();
                        break;
                    }
                    evt.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    setStatusText(ex.getMessage());
                }
            }
        };
    }

    private static boolean isInteger(final String string) {
        return string.chars().allMatch(Character::isDigit) && string.length() != 0;
    }

    private ISimpleEventSubscription<Image> decorateOnFinished(ImageWorker worker, final File file) {
        return (image) -> {
            System.out.println("Image worker finished!");
            if (image != null) {
                try {
                    System.out.println(image.getWidth(null) + "," + image.getHeight(null));
                    mode.setClipboard(image, file);
                    String status = "Copied smol image to clipboard, hehe! (" +
                            image.getWidth(null) +
                            ", " +
                            image.getHeight(null) +
                            ")";
                    setStatusText(status);
                } catch (IOException ex) {
                    setStatusText("Failed to paste file into clipboard. :c");
                }
            } else {
                setStatusText("Failed to create smol image. :c");
            }
            worker.finished.clear();
        };
    }

    private Supplier<Image> decorateScaleImage(final File file) {
        return () -> {
            URI imageURI = file.toURI();

            ImageIcon icon = null;
            try {
                icon = new ImageIcon(imageURI.toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return dimension.getScaledInstance(icon, width, height);
        };
    }
}
