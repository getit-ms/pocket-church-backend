/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.util;

import com.lowagie.text.pdf.ByteBuffer;
import com.lowagie.text.pdf.codec.Base64.InputStream;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.RandomAccess;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import lombok.AllArgsConstructor;

/**
 *
 * @author Gabriel
 */
public class PDFToImageConverterUtil {
    
    public static PDFConverter convert(File pdf){
        return new PDFConverter(pdf);
    }
    
    private final static int MAX = 2048;
    
    @AllArgsConstructor
    public static class PDFConverter {
        private File pdf;
        
        public void forEachPage(PageHandler handler) throws IOException {
            RandomAccessFile ra = new RandomAccessFile(pdf, "r");
            FileChannel channel = ra.getChannel();
            PDFFile pdffile = new PDFFile(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
            int numPags = pdffile.getNumPages();
            
            for (int i=1;i<=numPags;i++){
                PDFPage page = pdffile.getPage(i);
                
                double width;
                double height;
                if (page.getBBox().getWidth() > page.getBBox().getHeight()){
                    width = MAX;
                    height = page.getBBox().getHeight() * (MAX/page.getBBox().getWidth());
                }else{
                    width = page.getBBox().getWidth() * (MAX/page.getBBox().getHeight());
                    height = MAX;
                }
                
                Rectangle rect = new Rectangle(0, 0, (int) width, (int) height);
                
                Image img = page.getImage(rect.width, rect.height, 
                        new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight()), 
                        null, true, true);
                
                BufferedImage buffimg = toBufferedImage(img);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(buffimg, "png", baos);
                
                handler.handle(i, baos.toByteArray());
            }
        }
    }
    
    // This method returns a buffered image with the contents of an image
    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent
        // Pixels
        boolean hasAlpha = hasAlpha(image);
        // Create a buffered image with a format that's compatible with the
        // screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }
    
    private static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }
        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }
        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }
    
    public interface PageHandler {
        void handle(int page, byte[] data) throws IOException;
    }
    
}
