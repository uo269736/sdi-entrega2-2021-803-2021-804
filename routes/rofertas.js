//Función que exportamos como módulo
module.exports = function(app,swig,gestorBD) {

    /**
     * /oferta/agregar
     *
     * Carga la vista para agregar una oferta a base de datos
     */
    app.get('/oferta/agregar', function (req, res) {
        let respuesta = swig.renderFile('views/bagregar.html', {
            usuario : req.session.usuario,
            rol : req.session.rol,
            saldo : req.session.saldo
        });
        res.send(respuesta);
    });

    /**
     * /oferta/eliminar/:id
     *
     * Elimina una ofertacde base de datos al darle al botón eliminar, luego te redirige
     * a las ofertas propias de cada usuario
     */
    app.get('/oferta/eliminar/:id', function (req, res) {
        let criterio = {"_id" : gestorBD.mongo.ObjectID(req.params.id) };
        gestorBD.eliminarOferta(criterio,function(ofertas){
            if ( ofertas == null ){
                let respuestaError = swig.renderFile('views/error.html',
                    {
                        mensajes : "Error al eliminar",
                        usuario : req.session.usuario,
                        rol : req.session.rol,
                        saldo : req.session.saldo
                    });
                res.send(respuestaError);
            } else {
                res.redirect("/oferta/propias");
            }
        });
    });

    /**
     * /oferta/list
     *
     * Muestra una lista con todas las ofertas que hay en base de datos utilizando
     * la paginación
     */
    app.get("/oferta/list", function(req, res) {
        let criterio = {};
        if( req.query.busqueda != null ){
            criterio = { "titulo" : {$regex : ".*"+req.query.busqueda+".*", $options: "i"} };
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
                let ultimaPg = total/5;
                if (total % 5 > 0 ){ // Sobran decimales
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

    /**
     * /oferta/propias
     *
     * Lista las ofertas propias de cada usuario
     */
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

    /**
     * /oferta/comprar/:id
     *
     * Comprueba que puedas comprar una oferta y lo hace en el caso de que el usuario tenga saldo suficiente
     * y no sea el dueño de la oferta. Luego modifica el saldo de los usuarios implicados, tanto vendedor
     * como comprador
     */
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
                    if (isVendedor(ofertas[0], req.session.usuario)) {
                        let respuestaError = swig.renderFile('views/error.html',
                            {
                                mensajes : "No puedes comprar si eres el vendedor o ya la tienes comprada",
                                usuario : req.session.usuario,
                                rol : req.session.rol,
                                saldo : req.session.saldo
                            });
                        res.send(respuestaError);
                    } else {
                            if(!suficienteSaldo(ofertas[0],req.session.saldo)){
                                res.redirect("/oferta/list?mensaje=No tienes suficiente dinero para comprar esta oferta&tipoMensaje=alert-danger ");
                            }
                            else {
                                ofertas[0].comprador=req.session.usuario;
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
                                        let criterio = { "email" : req.session.usuario }
                                        gestorBD.actualizaSaldo(criterio,newSaldo,function (idUsuario){
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
                                                criterio = { "email" : ofertas[0].vendedor }
                                                gestorBD.obtenerUsuarios(criterio,function (usuarios) {
                                                    if (usuarios == null) {
                                                        let respuestaError = swig.renderFile('views/error.html',
                                                            {
                                                                mensajes: "Error al obtener vendedor",
                                                                usuario: req.session.usuario,
                                                                rol: req.session.rol,
                                                                saldo: req.session.saldo
                                                            });
                                                        res.send(respuestaError);
                                                    } else {
                                                        newSaldo=parseFloat(usuarios[0].saldo)+parseFloat(ofertas[0].precio);
                                                        gestorBD.actualizaSaldo(criterio,newSaldo,function (idUsuario) {
                                                            if (idUsuario == null) {
                                                                let respuestaError = swig.renderFile('views/error.html',
                                                                    {
                                                                        mensajes: "Error al procesar el pago",
                                                                        usuario: req.session.usuario,
                                                                        rol: req.session.rol,
                                                                        saldo: req.session.saldo
                                                                    });
                                                                res.send(respuestaError);
                                                            } else {
                                                                res.redirect("/oferta/list");
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }
        });
    });

    /**
     * /oferta/compradas
     *
     * Lista las ofertas compradas por el usuario que inició sesión
     */
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
                for(let i=0; i<compras.length; i++){
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

    /**
     * /oferta/destacar/:id
     *
     * Destaca una oferta en caso de que un usuario quiera y tenga dinero. Destacar hace que
     * la oferta aparezca en la zona privada del usuario justo después de iniciar sesión. Al igual que
     * comprar también actualiza el saldo del propietario de la oferta restandole 20€
     */
    app.get("/oferta/destacar/:id", function (req, res){
        let criterio = {
            vendedor : req.session.usuario,
            comprador :null
        };
        let respuestaError = swig.renderFile('views/error.html',
            {
                mensajes: "Error al destacar la oferta",
                usuario: req.session.usuario,
                rol: req.session.rol,
                saldo: req.session.saldo
            });
        if(req.session.saldo >= 20) {
            gestorBD.obtenerOfertas(criterio, function (ofertas) {
                if (ofertas == null) {
                    res.send(respuestaError);
                } else {
                    {
                        criterio = { "_id" : gestorBD.mongo.ObjectID(req.params.id) };
                        gestorBD.destacaOferta(criterio, true, function (result) {
                            if(result==null){
                                let respuestaError = swig.renderFile('views/error.html',
                                    {
                                        mensajes: "Error al destacar oferta.",
                                        usuario : req.session.usuario,
                                        rol : req.session.rol,
                                        saldo : req.session.saldo
                                    });
                                res.send(respuestaError);
                            }else{
                                let cantidad = req.session.saldo - 20;
                                req.session.saldo = cantidad;
                                let criterio = {"email": req.session.usuario};
                                gestorBD.actualizaSaldo(criterio, cantidad, function (result) {
                                    if (result == null) {
                                        let respuesta = swig.renderFile('views/error.html',
                                            {
                                                mensajes: "Error al modificar el saldo",
                                                usuario: req.session.usuario,
                                                rol: req.session.rol,
                                                saldo: req.session.saldo
                                            });
                                        res.send(respuesta);
                                    }
                                    else{
                                        res.redirect("/oferta/propias");
                                    }
                                });
                            }
                        })
                    }
                }
            });
        } else{
            res.send(respuestaError);
        }
    });

    /**
     * /oferta/:id
     *
     * Carga los detalles de una oferta en concreto, mostrando todos sus datos
     */
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
                            usuario : req.session.usuario,
                            rol : req.session.rol,
                            saldo : req.session.saldo,
                            oferta: ofertas[0],
                            propietario: isVendedorOrComprada(compras, ofertas[0], req.session.usuario)
                        });
                    res.send(respuesta);
                });
            }
        });
    });

    /**
     * /oferta
     *
     * Inserta una oferta en base de datos cuando se completa el formulario que está
     * en la vista de agregar oferta. Valida los campos antes de agregarla
     */
    app.post("/oferta", function (req, res){
        let d=new Date();
        let oferta = {
            titulo : req.body.nombre,
            descripcion : req.body.descripcion,
            precio : req.body.precio,
            fecha : d.getDate()+"/"+(d.getMonth()+1)+"/"+d.getFullYear(),
            vendedor: req.session.usuario,
            comprador : null,
            destacada : (req.body.destacada==="on")
        }
        let mensaje=validacionAgregarOferta(oferta);
        if(mensaje==="") {
            gestorBD.insertarOferta(oferta, function(id){
                if (id == null) {
                    let respuestaError = swig.renderFile('views/error.html',
                        {
                            mensajes : "Error al insertar oferta",
                            usuario : req.session.usuario,
                            rol : req.session.rol,
                            saldo : req.session.saldo
                        });
                    res.send(respuestaError);
                } else {
                    if(req.body.destacada==="on") {
                        let cantidad = req.session.saldo - 20;
                        req.session.saldo = cantidad;
                        let criterio = {"email": req.session.usuario};
                        gestorBD.actualizaSaldo(criterio, cantidad, function (result) {
                            if (result == null) {
                                let respuesta = swig.renderFile('views/error.html',
                                    {
                                        mensajes: "Error al modificar el saldo",
                                        usuario: req.session.usuario,
                                        rol: req.session.rol,
                                        saldo: req.session.saldo
                                    });
                                res.send(respuesta);
                            }else{
                                res.redirect("/oferta/propias");
                            }
                        });
                    }
                }
            });
        }else{
            res.redirect("/oferta/agregar" +
                "?mensaje="+mensaje+"&tipoMensaje=alert-danger ");
        }
    });

    /**
     * Comprueba que el usuario no sea el vendedor de la oferta ni que la oferta
     * ya este comprada
     * @param compras Ofertas con el atributo de comprada a distinto de null
     * @param oferta Oferta a comprobar
     * @param usuario Usuario que quiere comprar la oferta
     * @returns {boolean} Devuelve True si es el vendedor o ya esta comprada y False si no
     */
    function isVendedorOrComprada(compras, oferta, usuario) {
        if ((oferta.vendedor) === usuario) {
            return true;
        } else {
            for (let i = 0; i < compras.length; i++) {
                if ((compras[i]._id).equals(oferta._id)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Comprueba que el usuario no sea el vendedor de la oferta
     * @param oferta Oferta a comprobar
     * @param usuario Usuario que quiere comprar la oferta
     * @returns {boolean} True si es el vendedor, False si no
     */
    function isVendedor(oferta, usuario) {
        return oferta.vendedor === usuario;
    }

    /**
     * Comprueba que el saldo del usuario sea mayor o igual que el precio de la
     * oferta que se desea comprar
     * @param oferta Oferta que se quiere comprar
     * @param saldo Saldo del usuario que quiere comprar la oferta
     * @returns {boolean} True si tiene y False si no
     */
    function suficienteSaldo(oferta, saldo) {
        return oferta.precio <= saldo;
    }

    /**
     * Validación para comprobar los campos a la hora de agregar una oferta
     * @param oferta Oferta que se quiere agregar
     * @returns {string} Devuelve una cadena con los errores en el caso de que haya.
     */
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
    }
};
