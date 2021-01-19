/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.app;

import br.gafs.pocket.corporate.app.dto.ErrosDTO;
import br.gafs.pocket.corporate.exception.ValidationException;
import br.gafs.exceptions.ServiceException;
import br.gafs.exceptions.ServiceExceptionList;
import br.gafs.util.exception.ExceptionUnwrapperUtil;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Gabriel
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class RestExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e0) {
        ErrosDTO dto;
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        
        try{
            e0.printStackTrace();
            
            throw ExceptionUnwrapperUtil.unwrappException(e0);
        }catch(ValidationException e){
            status = Response.Status.BAD_REQUEST;
            dto = new ErrosDTO(e);
        }catch(ServiceException e){
            status = Response.Status.BAD_REQUEST;
            dto = new ErrosDTO(e);
        }catch(SecurityException e){
            status = Response.Status.FORBIDDEN;
            dto = new ErrosDTO(e);
        }catch(ServiceExceptionList e){
            status = Response.Status.BAD_REQUEST;
            dto = new ErrosDTO(e);
        }catch(ConstraintViolationException e){
            status = Response.Status.BAD_REQUEST;
            dto = new ErrosDTO(e);
        }catch(Throwable e){
            dto = new ErrosDTO(e.getMessage());
        }
        
        return Response.status(status).entity(dto).
                type(MediaType.APPLICATION_JSON).build();
    }
}
