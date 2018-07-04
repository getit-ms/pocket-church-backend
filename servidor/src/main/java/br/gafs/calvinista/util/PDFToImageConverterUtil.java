/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.util;

import br.gafs.util.image.ImageUtil;
import lombok.AllArgsConstructor;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.RendererException;
import org.ghost4j.renderer.SimpleRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


/**
 *
 * @author Gabriel
 */
public class PDFToImageConverterUtil {
    private static final double LIMIT_HEIGHT = 1000;
    private static final double LIMIT_WIDTH = 1000;

    public static PDFConverter convert(File pdf, int index, int limit){
        return new PDFConverter(pdf, index, limit);
    }

    @AllArgsConstructor
    public static class PDFConverter {
        private File pdf;
        private int index;
        private int limit;


        public int forEachPage(PageHandler handler) throws IOException {
            PDFDocument document = new PDFDocument();

            document.load(pdf);

            // create renderer
            SimpleRenderer renderer = new SimpleRenderer();

            int pageCount;
            try {
                pageCount = document.getPageCount();

                int total = limit > 0 ? Math.min(pageCount, index + limit) : pageCount;

                for (int i=index;i<total;i++) {
                    BufferedImage image = getBufferedImage(document, renderer, i);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    ImageIO.write(image, "png", baos);

                    handler.handle(i, baos.toByteArray());
                }
            } catch (RendererException | DocumentException e) {
                throw new IOException(e);
            }

            return pageCount;
        }

        private BufferedImage getBufferedImage(PDFDocument document, SimpleRenderer renderer, int i) throws IOException, RendererException, DocumentException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Small resolution to discover width and height
            renderer.setResolution(10);

            Image image = renderer.render(document, i, i).get(0);

            renderer.setResolution((int) Math.min((10 * LIMIT_WIDTH) / image.getWidth(null), (10 * LIMIT_HEIGHT) / image.getHeight(null)));

            image  = renderer.render(document, i, i).get(0);

            return createBufferedImage(image, BufferedImage.TYPE_INT_RGB);
        }

        private static BufferedImage createBufferedImage(Image imageIn, int imageType) {
            if(imageIn instanceof BufferedImage) {
                return (BufferedImage)imageIn;
            } else {
                BufferedImage bufferedImageOut = new BufferedImage(
                        imageIn.getWidth((ImageObserver)null),
                        imageIn.getHeight((ImageObserver)null), imageType);
                Graphics g = bufferedImageOut.getGraphics();
                g.drawImage(imageIn, 0, 0, (ImageObserver)null);
                return bufferedImageOut;
            }
        }
    }

    public interface PageHandler {
        void handle(int page, byte[] data) throws IOException;
    }

}
