/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.calvinista.util;

import lombok.AllArgsConstructor;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.RendererException;
import org.ghost4j.renderer.SimpleRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 *
 * @author Gabriel
 */
public class PDFToImageConverterUtil {

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
            renderer.setResolution(300);

            // render
            int pageCount;
            List<Image> images;
            try {
                pageCount = document.getPageCount();
                images = renderer.render(document, index, limit > 0 ? Math.min(pageCount, index + limit) : pageCount);
            } catch (RendererException | DocumentException e) {
                throw new IOException(e);
            }

            // write images to files to disk as PNG
            for (int i = 0; i < images.size(); i++) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write((RenderedImage) images.get(i), "png", baos);
                handler.handle(index + i, baos.toByteArray());
            }

            return pageCount;
        }
    }

    public interface PageHandler {
        void handle(int page, byte[] data) throws IOException;
    }

}
