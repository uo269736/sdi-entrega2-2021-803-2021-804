module.exports = function(app, gestorBD) {
    app.post("/api/autenticar/", function(req, res) {
        let seguro = app.get("crypto").createHmac('sha256', app.get('clave'))
            .update(req.body.password).digest('hex');
        let criterio = {
            email : req.body.email,
            password : seguro
        }
        // ¿Validar nombre, genero, precio?

        gestorBD.obtenerUsuarios(criterio, function(usuarios){
            if (usuarios == null || usuarios.length == 0) {
                res.status(401);    // Usuario no autorizado
                res.json({
                    autenticado : false
                })
            } else {
                let token = app.get('jwt').sign(
                    {usuario: criterio.email , tiempo: Date.now()/1000},
                    "secreto");
                res.status(200);
                res.json({
                    autenticado : true,
                    token : token
                })
            }
        });

    });

    app.get("/api/oferta", function(req, res) {
        gestorBD.obtenerOfertas( {} , function(ofertas) {
            if (ofertas == null) {
                res.status(500);
                res.json({
                    error : "se ha producido un error"
                })
            } else {
                res.status(200);
                res.send( JSON.stringify(ofertas) );
            }
        });
    });

    app.get("/api/oferta/:id", function(req, res) {
        let criterio = { "_id" : gestorBD.mongo.ObjectID(req.params.id)}

        gestorBD.obtenerOfertas(criterio,function(ofertas){
            if ( ofertas == null ){
                res.status(500);
                res.json({
                    error : "se ha producido un error"
                })
            } else {
                res.status(200);
                res.send( JSON.stringify(ofertas[0]) );
            }
        });
    });

    function propietario(req, criterio){
        gestorBD.obtenerOfertas(criterio,function(ofertas) {
            if (req.session.usuario == null || ofertas == null)
                return false;
            else {
                return req.session.usuario == (ofertas[0].vendedor);
            }
        });
    }
};