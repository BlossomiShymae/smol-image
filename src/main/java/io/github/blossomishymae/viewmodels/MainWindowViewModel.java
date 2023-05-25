package io.github.blossomishymae.viewmodels;

import com.google.inject.Singleton;
import io.github.blossomishymae.componentmodel.ObservableObject;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Singleton
public class MainWindowViewModel extends ObservableObject {
    private static class ImageSelection implements Transferable {
        private final Image image;

        public ImageSelection(Image image) {
            this.image = image;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.imageFlavor };
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

    private int width;
    private int height;
    private String statusText = "";

    public  MainWindowViewModel() {
        super();
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
                        Image image = ImageIO.read(file);
                        Image scaledImage = image.getScaledInstance(width, -1, Image.SCALE_SMOOTH);
                        setClipboard(scaledImage);
                        setStatusText("Copied smol image to clipboard! <3");
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

    private static void setClipboard(final Image image) {
        ImageSelection imageSelection = new ImageSelection(image);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageSelection, null);
    }
}
