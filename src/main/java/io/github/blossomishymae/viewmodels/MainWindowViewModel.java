package io.github.blossomishymae.viewmodels;

import com.google.inject.Singleton;
import io.github.blossomishymae.componentmodel.ObservableObject;
import io.github.blossomishymae.event.ISimpleEventSubscription;
import io.github.blossomishymae.event.SimpleEventHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

@Singleton
public class MainWindowViewModel extends ObservableObject {
    public enum Dimension {
        NONE,
        WIDTH,
        HEIGHT
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
    private Dimension dimension = Dimension.NONE;
    private String statusText = "";

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

    public void setWidth(final int width) {
        this.width = width;
        notifyPropertyChanged("setWidth");
    }

    public void setHeight(final int height) {
        this.height = height;
        notifyPropertyChanged("setHeight");
    }

    public void setStatusText(final String statusText) {
        this.statusText = statusText;
        notifyPropertyChanged("setStatusText");
    }

    public void setDimension(final Dimension dimension) {
        this.dimension = dimension;
        notifyPropertyChanged("setDimension");
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
                        worker.finished.subscribe(decorateOnFinished(worker));
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

    private ISimpleEventSubscription<Image> decorateOnFinished(ImageWorker worker) {
        return (image) -> {
            System.out.println("Image worker finished!");
            if (image != null) {
                setClipboard(image);
                String status = "Copied smol image to clipboard, hehe! (" +
                        image.getWidth(null) +
                        ", " +
                        image.getHeight(null) +
                        ")";
                setStatusText(status);
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
            Image scaledImage = null;
            switch (dimension) {
                case WIDTH -> scaledImage = icon.getImage().getScaledInstance(width, -1, Image.SCALE_SMOOTH);
                case HEIGHT -> scaledImage = icon.getImage().getScaledInstance(-1, height, Image.SCALE_SMOOTH);
            }
            return scaledImage;
        };
    }

    private static void setClipboard(final Image image) {
        ImageSelection imageSelection = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageSelection, null);
    }
}
