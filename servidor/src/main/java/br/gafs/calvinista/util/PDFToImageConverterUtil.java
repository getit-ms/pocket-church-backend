/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.RendererException;
import org.ghost4j.renderer.SimpleRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Gabriel
 */
public class PDFToImageConverterUtil {
    private static final double LIMIT_HEIGHT = 1000;
    private static final double LIMIT_WIDTH = 1000;

    private static final long TIMEOUT = 10000;

    private final static Logger LOGGER = Logger.getLogger(PDFToImageConverterUtil.class.getSimpleName());

    public static PDFConverter convert(File pdf, int index, int limit){
        return new PDFConverter(pdf, index, limit);
    }
    @AllArgsConstructor
    public static class PDFConverter {
        private File pdf;
        private int index;
        private int limit;


        public int forEachPage(PageHandler handler) throws IOException {
            LOGGER.info("Abrindo documento " + pdf.getName());

            PDFDocument document = new PDFDocument();

            document.load(pdf);

            // create renderer
            SimpleRenderer renderer = new SimpleRenderer();

            int pageCount;
            try {
                pageCount = document.getPageCount();

                int total = limit > 0 ? Math.min(pageCount, index + limit) : pageCount;

                for (int i=index;i<total;i++) {
                    long inicio = System.currentTimeMillis();

                    LOGGER.info("Iniciando processamento de " + pdf.getName() + " - " + (i+1));

                    BufferedImage image = getBufferedImage(document, renderer, i);

                    LOGGER.info("Processamento de " + pdf.getName() + " - " + (i+1) + " levou " + (System.currentTimeMillis() - inicio) + " millis.");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    ImageIO.write(image, "png", baos);

                    handler.handle(i, baos.toByteArray());
                }
            } catch (Exception e) {
                throw new IOException(e);
            }

            return pageCount;
        }

        private BufferedImage getBufferedImage(PDFDocument document, SimpleRenderer renderer, int i) throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Small resolution to discover width and height
            renderer.setResolution(10);

            Image image = renderer.render(document, i, i).get(0);

            renderer.setResolution((int) Math.min((10 * LIMIT_WIDTH) / image.getWidth(null), (10 * LIMIT_HEIGHT) / image.getHeight(null)));

            LOGGER.info("Preparando para renderizar " + pdf.getName() + " pag " + (i+1) + " a " + renderer.getResolution() + " DPIs");

            image  = renderWithTimeout(new RendererRunnable(document, renderer, i));

            return createBufferedImage(image, BufferedImage.TYPE_INT_RGB);
        }

        private Image renderWithTimeout(RendererRunnable rendererRunnable) throws Exception {
            Thread t = new Thread(rendererRunnable);

            t.start();

            t.join(TIMEOUT);

            if (t.isAlive()) {
                t.interrupt();
                throw new TimeoutException("Tempo de espera para renderização espirou.");
            } else if (rendererRunnable.getCause() != null) {
                throw rendererRunnable.getCause();
            } else if (rendererRunnable.getImage() == null) {
                throw new RuntimeException("Houve problemas para renderizar o PDF");
            }

            return rendererRunnable.getImage();
        }

        @RequiredArgsConstructor
        class RendererRunnable implements Runnable {
            private final PDFDocument document;
            private final SimpleRenderer renderer;
            private final int i;

            @Getter
            private Image image;

            @Getter
            private Exception cause;

            @Override
            public void run() {
                try {
                    this.image = renderer.render(document, i, i).get(0);
                } catch (Exception e) {
                    this.cause = e;
                }
            }
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
