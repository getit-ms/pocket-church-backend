/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.gafs.pocket.corporate.app.servlet;

import br.gafs.pocket.corporate.service.AppService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gabriel
 */
@WebServlet(urlPatterns = "/ipa/*")
public class IPAServlet extends HttpServlet {
    private final static Logger LOGGER = Logger.getLogger(IPAServlet.class.getName());
    
    @EJB
    private AppService appService;
    
    @Override
    protected void doGet(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String path = req.getRequestURI().replaceAll("(.+/)?ajuda/?", "");

        File file = appService.buscaIPA(path);

        resp.addHeader("Content-Type", "application/octet-stream");
        resp.addHeader("Content-Length", "" + file.length());
        resp.addHeader("Content-Disposition", "attachment; filename=\""+file.getName()+"\"");
        
        transfer(new FileInputStream(file), resp.getOutputStream());
    }
    
    private void transfer(InputStream is, OutputStream os){
        try{
            int size;
            byte[] cache = new byte[5000];
            while ((size = is.read(cache)) > 0){
                os.write(cache, 0, size);
                os.flush();
            }

            is.close();
            os.close();
        }catch(Exception e){
            LOGGER.log(Level.SEVERE, "Problema ao transferir dados", e);
        }
    }
}
