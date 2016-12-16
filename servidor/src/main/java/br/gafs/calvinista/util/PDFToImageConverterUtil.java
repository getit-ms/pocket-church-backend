/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.util;

import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

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
        
        public int forEachPage(PageHandler handler) throws IOException {
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

                return numPags;
            }finally{
                pdffile.close();
            }
        }
    }
    
    public interface PageHandler {
        void handle(int page, byte[] data) throws IOException;
    }
    
}
