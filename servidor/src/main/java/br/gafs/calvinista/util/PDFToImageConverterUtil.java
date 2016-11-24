/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.util;

import com.lowagie.text.pdf.ByteBuffer;
import com.lowagie.text.pdf.codec.Base64.InputStream;
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
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 *
 * @author Gabriel
 */
public class PDFToImageConverterUtil {
    
    public static PDFConverter convert(File pdf){
        return new PDFConverter(pdf);
    }
    
    @AllArgsConstructor
    public static class PDFConverter {
        private File pdf;
        
        public void forEachPage(PageHandler handler) throws IOException {
            PDDocument pdffile = PDDocument.load(pdf);
            try{
                int numPags = pdffile.getNumberOfPages();
                PDFRenderer renderer = new PDFRenderer(pdffile);

                for (int i=0;i<numPags;i++){
                    BufferedImage img = renderer.renderImage(i, 3, ImageType.RGB);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "png", baos);

                    handler.handle(i, baos.toByteArray());
                }
            }finally{
                pdffile.close();
            }
        }
    }
    
    public interface PageHandler {
        void handle(int page, byte[] data) throws IOException;
    }
    
}
