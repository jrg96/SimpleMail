/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clases;

import clases.protocolomail.MensajeComprobarDestinatario;
import clases.protocolomail.MensajeEnviarCorreo;
import clases.protocolomail.MensajeInicioSesion;
import clases.protocolomail.ProtocoloMail;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import servidormail.formularios.frmPantallaPrincipal;

/**
 *
 * @author home
 */
public class ConexionCliente extends Thread {
    private Socket cliente;
    private boolean continuar = true;
    private InputStream entrada;
    private OutputStream salida;
    private frmPantallaPrincipal padre;
    
    public ConexionCliente(frmPantallaPrincipal padre, Socket cliente){
        this.padre = padre;
        this.setCliente(cliente);
    }
    
    public void setCliente(Socket cliente){
        this.cliente = cliente;
    }
    
    public void setContinuar(boolean val){
        this.continuar = val;
    }
    
    public boolean getContinuar(){
        return this.continuar;
    }
    
    public boolean chequearLogin(MensajeInicioSesion msj){
        ElementoListaUsuarios usuario = this.padre.tablaUsuarios.buscarUsuario(msj.getEmail());
                
        if (usuario != null){
            if (usuario.getUsuario().getPassword().equals(msj.getPassword())){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    public boolean buscarDestinatario(MensajeComprobarDestinatario msj){
        ElementoListaUsuarios usuario = this.padre.tablaUsuarios.buscarUsuario(msj.getEmail());
        
        if (usuario != null){
            return true;
        }
        return false;
    }
    
    public void run(){
        try {
            entrada = cliente.getInputStream();
            salida = cliente.getOutputStream();
            
            while (this.getContinuar()){
                int ID = entrada.read();
                
                switch(ID){
                    case ProtocoloMail.INICIO_SESION:
                        MensajeInicioSesion msj = ProtocoloMail.procesarInicioSesion(entrada);
                        if (chequearLogin(msj)){
                            salida.write(ProtocoloMail.SESION_ACEPTADA);
                        } else {
                            salida.write(ProtocoloMail.SESION_RECHAZADA);
                        }
                        break;
                    case ProtocoloMail.BUSCAR_DESTINATARIO:
                        MensajeComprobarDestinatario msj2 = ProtocoloMail.procesarCompDestinatario(entrada);
                        if (this.buscarDestinatario(msj2)){
                            salida.write(ProtocoloMail.DESTINATARIO_ENCONTRADO);
                        } else {
                            salida.write(ProtocoloMail.DESTINATARIO_DESCONOCIDO);
                        }
                        break;
                    case ProtocoloMail.ENVIAR_CORREO:
                        MensajeEnviarCorreo msj3 = ProtocoloMail.procesarEnvioCorreo(entrada);
                        System.out.println("Remitente: " + msj3.getRemitente());
                        System.out.println("Destinatario: " + msj3.getDestinatario());
                        System.out.println("Mensaje: " + msj3.getMensaje());
                        System.out.println("Tamaño de adjunto: " + msj3.getDatos().length);
                        System.out.println("Tamaño de bytes de firma: " + msj3.getFirmaDigital().length);
                        break;
                    case -1:
                        this.setContinuar(false);
                        this.cliente.close();
                        break;
                    default:
                        System.out.println("Error al leer id, se ha leido id desconocido de: " + ID);
                        break;
                }
            }
        } catch(Exception e){
            System.out.println("Error al leer datos!!!");
            System.out.println("" + e.toString());
            this.setContinuar(false);
        }
    }
}
