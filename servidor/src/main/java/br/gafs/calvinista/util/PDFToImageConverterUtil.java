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
import java.awt.image.BufferedImage;
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
    private static final int LIMIT_HEIGHT = 900;
    private static final int LIMIT_WIDTH = 900;

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

            // set resolution (in DPI)
            renderer.setResolution(220);

            // render

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

            ImageIO.write((RenderedImage) renderer.render(document, i, i).get(0), "png", baos);

            return ImageIO.read(new ByteArrayInputStream(ImageUtil.redimensionaImagem(baos.toByteArray(), "png", LIMIT_HEIGHT, LIMIT_WIDTH)));
        }
    }

    public interface PageHandler {
        void handle(int page, byte[] data) throws IOException;
    }

}
