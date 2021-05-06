//Función que exportamos como módulo
module.exports = function(app,swig,gestorBD) {


    app.get('/oferta/agregar', function (req, res) {
        let respuesta = swig.renderFile('views/bagregar.html', {
            usuario : req.session.usuario,
            rol : req.session.rol,
            saldo : req.session.saldo
        });
        res.send(respuesta);
    });

    app.get('/oferta/eliminar/:id', function (req, res) {
        let criterio = {"_id" : gestorBD.mongo.ObjectID(req.params.id) };
        gestorBD.eliminarOferta(criterio,function(ofertas){
            if ( ofertas == null ){
                res.send(respuesta);
            } else {
                res.redirect("/oferta/propias");
            }
        });
    });

    app.get("/oferta/list", function(req, res) {
        let criterio = {};
        if( req.query.busqueda != null ){
            criterio = { "titulo" : {$regex : ".*"+req.query.busqueda+".*"} };
        }

        let pg = parseInt(req.query.pg); // Es String !!!
        if ( req.query.pg == null){ // Puede no venir el param
            pg = 1;
        }

        gestorBD.obtenerOfertasPg(criterio, pg , function(ofertas, total ) {
            if (ofertas == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let ultimaPg = total/4;
                if (total % 4 > 0 ){ // Sobran decimales
                    ultimaPg = ultimaPg+1;
                }
                let paginas = []; // paginas mostrar
                for(let i = pg-2 ; i <= pg+2 ; i++){
                    if ( i > 0 && i <= ultimaPg){
                        paginas.push(i);
                    }
                }
                let respuesta = swig.renderFile('views/bofertas.html',
                    {
                        ofertas : ofertas,
                        paginas : paginas,
                        actual : pg,
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuesta);
            }
        });
    });

    app.get("/oferta/propias", function(req, res) {
        let criterio = { vendedor : req.session.usuario };
        gestorBD.obtenerOfertas(criterio, function(ofertas) {
            if (ofertas == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let respuesta = swig.renderFile('views/bpropias.html',
                    {
                        ofertas : ofertas,
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuesta);
            }
        });
    });

    app.get('/oferta/comprar/:id', function (req, res) {
        let criterioOferta = {"_id": gestorBD.mongo.ObjectID(req.params.id)};
        gestorBD.obtenerOfertas(criterioOferta, function (ofertas) {
            if (ofertas == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al recuperar la oferta",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let criterioCompra = {"usuario": req.session.usuario};
                gestorBD.obtenerCompras(criterioCompra, function (compras) {
                    if (compras == null) {
                        let respuestaError = swig.renderFile('views/error.html',
                            {
                                mensajes : "Error al listar",
                                usuario : req.session.usuario,
                                rol : req.session.rol,
                                saldo : req.session.saldo
                            });
                        res.send(respuestaError);
                    } else {
                        if (isVendedorOrComprada(compras, ofertas[0], req.session.usuario)) {
                            let respuestaError = swig.renderFile('views/error.html',
                                {
                                    mensajes : "No puedes comprar si eres el autor o ya la tienes comprada",
                                    usuario : req.session.usuario,
                                    rol : req.session.rol,
                                    saldo : req.session.saldo
                                });
                            res.send(respuestaError);
                        } else {
                            if(!suficienteSaldo(ofertas[0],req.session.saldo)){
                                res.redirect("/oferta/list?mensaje=No tienes suficiente dinero para comprar esta oferta");
                            }
                            else {
                                oferta[0].comprador=req.session.usuario;
                                gestorBD.comprarOferta(criterioOferta,ofertas[0], function (idCompra) {
                                    if (idCompra == null) {
                                        let respuestaError = swig.renderFile('views/error.html',
                                            {
                                                mensajes : "Error al comprar la oferta",
                                                usuario : req.session.usuario,
                                                rol : req.session.rol,
                                                saldo : req.session.saldo
                                            });
                                        res.send(respuestaError);
                                    } else {
                                        let newSaldo=req.session.saldo-ofertas[0].precio;
                                        req.session.saldo=newSaldo;
                                        gestorBD.actualizaSaldo(req.session.usuario,newSaldo,function (idUsuario){
                                            if(idUsuario==null){
                                                let respuestaError = swig.renderFile('views/error.html',
                                                    {
                                                        mensajes : "Error al procesar el pago",
                                                        usuario : req.session.usuario,
                                                        rol : req.session.rol,
                                                        saldo : req.session.saldo
                                                    });
                                                res.send(respuestaError);
                                            } else {
                                                res.redirect("/oferta/list");
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }
                });
            };
        });
    });

    app.get('/oferta/compradas', function (req, res) {
        let criterio = { comprador : req.session.usuario };
        gestorBD.obtenerCompras(criterio ,function(compras){
            if ( compras == null ){
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al listar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                let ofertasCompradasIds = [];
                for(i=0; i<compras.length; i++){
                    ofertasCompradasIds.push( compras[i]._id );
                }
                let criterio = { "_id" : { $in: ofertasCompradasIds } }
                gestorBD.obtenerOfertas(criterio,function (ofertas){
                    let respuesta = swig.renderFile('views/bcompradas.html',
                        {
                            ofertas : ofertas,
                            usuario : req.session.usuario,
                            rol : req.session.rol,
                            saldo : req.session.saldo
                        });
                    res.send(respuesta);
                });
            }
        });
    });

    app.get('/oferta/:id', function(req, res) {
        let criterio = {"_id": gestorBD.mongo.ObjectID(req.params.id)};
        gestorBD.obtenerOfertas(criterio, function (ofertas) {
            if (ofertas == null) {
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes: "Error al recuperar la oferta.",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                    criterio = {"usuario": req.session.usuario};
                    gestorBD.obtenerCompras(criterio, function (compras) {
                        let respuesta = swig.renderFile('views/boferta.html',
                            {
                                oferta: ofertas[0],
                                propietario: isVendedorOrComprada(compras, ofertas[0], req.session.usuario)
                            });
                        res.send(respuesta);
                    });
                //});
            }
        });
    });

    app.post("/oferta", function (req, res){
        let d=new Date();
        let oferta = {
            titulo : req.body.nombre,
            descripcion : req.body.descripcion,
            precio : req.body.precio,
            fecha : d.getDate()+"/"+(d.getMonth()+1)+"/"+d.getFullYear(),
            vendedor: req.session.usuario,
            comprador : null
        }
        let mensaje=validacionAgregarOferta(oferta);
        if(mensaje=="") {
            gestorBD.insertarOferta(oferta, function (id) {
                if (id == null) {
                    let respuestaError = swig.renderFile('views/error.html',
                        {
                            mensajes: "Error al insertar oferta",
                            usuario: req.session.usuario,
                            rol: req.session.rol,
                            saldo: req.session.saldo
                        });
                    res.send(respuestaError);
                } else {
                    res.redirect("/oferta/propias");
                }
            });
        }else{
            res.redirect("/oferta/agregar" +
                "?mensaje="+mensaje+"&tipoMensaje=alert-danger ");
        }
    });

    function isVendedorOrComprada(compras, oferta, usuario) {
        if ((oferta.vendedor) == usuario) {
            return true;
        } else {
            for (let i = 0; i < compras.length; i++) {
                if ((compras[i]._id).equals(oferta._id)) {
                    return true;
                }
            }
            return false;
        }
    };

    function suficienteSaldo(oferta, saldo) {
        if (oferta.precio > saldo){
            return false;
        } else {
            return true;
        }
    };

    function validacionAgregarOferta(oferta) {
        let mensaje="";
        if (oferta.titulo.length<4){
            mensaje+="El titulo debe tener al menos 4 caracteres<br>";
        }
        if (oferta.descripcion.length<5){
            mensaje+="La descripción debe tener al menos 5 caracteres<br>";
        }
        if (oferta.precio<0){
            mensaje+="El precio no puede ser negativo<br>";
        }
        return mensaje;
    };
};
