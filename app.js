//Módulos //Base de datos (nombre:admin,contraseña:sdi)
let express = require('express');
let app = express();

let rest = require('request');
app.set('rest',rest);

app.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Credentials", "true");
    res.header("Access-Control-Allow-Methods", "POST, GET, DELETE, UPDATE, PUT");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, token");
    // Debemos especificar todas las headers que se aceptan. Content-Type , token
    next();
});

let jwt = require('jsonwebtoken');
app.set('jwt',jwt);

let fs = require('fs');
let https = require('https');


let expressSession = require('express-session');
app.use(expressSession({
    secret: 'abcdefg',
    resave: true,
    saveUninitialized: true
}));
let crypto = require('crypto');

let fileUpload = require('express-fileupload');
app.use(fileUpload());
let mongo = require('mongodb');
let swig = require('swig');
let bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

let gestorBD = require("./modules/gestorBD.js");
gestorBD.init(app,mongo);

// routerUsuarioToken
let routerUsuarioToken = express.Router();
routerUsuarioToken.use(function(req, res, next) {
    // obtener el token, vía headers (opcionalmente GET y/o POST).
    let token = req.headers['token'] || req.body.token || req.query.token;
    if (token != null) {
        // verificar el token
        jwt.verify(token, 'secreto', function(err, infoToken) {
            if (err || (Date.now()/1000 - infoToken.tiempo) > 240 ){
                res.status(403); // Forbidden
                res.json({
                    acceso : false,
                    error: 'Token invalido o caducado'
                });

            } else {
                // dejamos correr la petición
                res.usuario = infoToken.usuario;
                next();
            }
        });

    } else {
        res.status(403); // Forbidden
        res.json({
            acceso : false,
            mensaje: 'No hay Token'
        });
    }
});

// Aplicar routerUsuarioToken
app.use('/api/oferta', routerUsuarioToken);
app.use('/api/conversaciones', routerUsuarioToken);
app.use('/api/chat', routerUsuarioToken);
app.use('/api/borrar', routerUsuarioToken);

// routerUsuarioSession
var routerUsuarioSession = express.Router();
routerUsuarioSession.use(function(req, res, next) {
    console.log("routerUsuarioSession");
    if ( req.session.usuario ) {
        // dejamos correr la petición
        next();
    } else {
        console.log("va a : "+req.session.destino)
        res.redirect("/identificarse");
    }
});

//Aplicar routerUsuarioSession
app.use("/oferta/agregar",routerUsuarioSession);
app.use("/oferta/propias",routerUsuarioSession);
app.use("/oferta/list",routerUsuarioSession);
app.use("/oferta/compradas",routerUsuarioSession);
app.use("/oferta/comprar",routerUsuarioSession);
app.use("/home",routerUsuarioSession);


//routerUsuarioVendedor
let routerUsuarioVendedor = express.Router();
routerUsuarioVendedor.use(function(req, res, next) {
    console.log("routerUsuarioVendedor");
    let path = require('path');
    let id = path.basename(req.originalUrl);
// Cuidado porque req.params no funciona
// en el router si los params van en la URL.
    gestorBD.obtenerOfertas(
        {_id: mongo.ObjectID(id) }, function (ofertas) {
            console.log(ofertas[0]);
            if(ofertas[0].vendedor === req.session.usuario ){
                next();
            } else {
                res.redirect("/oferta/propias");
            }
        })
});

//Aplicar routerUsuarioVendedor
app.use("/oferta/eliminar",routerUsuarioVendedor);
app.use("/oferta/destacar",routerUsuarioVendedor);


//routerUsuarioIniciarSesion
let routerUsuarioIniciarSesion = express.Router();
routerUsuarioIniciarSesion.use(function(req, res, next) {
    console.log("routerUsuarioIniciarSesion");
    if ( !req.session.usuario) {
        // dejamos correr la petición
        next();
    } else {
        console.log("va a : "+req.session.destino)
        res.redirect("/home");
    }
});

//Aplicar routerAdmin
app.use("/registrarse",routerUsuarioIniciarSesion);
app.use("/identificarse",routerUsuarioIniciarSesion);


//routerUsuarioAdmin
let routerUsuarioAdmin = express.Router();
routerUsuarioAdmin.use(function(req, res, next) {
    console.log("routerUsuarioAdmin");
    if ( req.session.rol==="admin" ) {
        // dejamos correr la petición
        next();
    } else {
        console.log("va a : "+req.session.destino)
        res.redirect("/identificarse");
    }
});

//Aplicar routerAdmin
app.use("/usuario/list",routerUsuarioAdmin);
app.use("/usuario/eliminar",routerUsuarioAdmin);


//routerUsuarioEstandar
let routerUsuarioEstandar = express.Router();
routerUsuarioEstandar.use(function(req, res, next) {
    console.log("routerUsuarioEstandar");
    if ( req.session.rol==="usuario" ) {
        // dejamos correr la petición
        next();
    } else {
        console.log("va a : "+req.session.destino)
        res.redirect("/home");
    }
});

//Aplicar routerAdmin
app.use("/oferta/agregar",routerUsuarioEstandar);
app.use("/oferta/propias",routerUsuarioEstandar);
app.use("/oferta/list",routerUsuarioEstandar);
app.use("/oferta/compradas",routerUsuarioEstandar);

//LOGGER
const log4js = require("log4js");
log4js.configure({
    appenders: { myWallapop: { type: "file", filename: "myWallapopLogger.log" } },
    categories: { default: { appenders: ["myWallapop"], level: "info" } }
});
const logger = log4js.getLogger("myWallapop");

app.use(express.static('public'));

//Variables
app.set('port',8081);
app.set('db','mongodb://admin:sdi@mywallapop-shard-00-00.thyhc.mongodb.net:27017,mywallapop-shard-00-01.thyhc.mongodb.net:27017,mywallapop-shard-00-02.thyhc.mongodb.net:27017/myFirstDatabase?ssl=true&replicaSet=atlas-7nrj2v-shard-0&authSource=admin&retryWrites=true&w=majority');
app.set('clave','abcdefg');
app.set('crypto',crypto);
//Creamos una variable para el logger
app.set('logger',logger);

//Rutas/controladores por lógica
require("./routes/rusuarios.js")(app,swig,gestorBD); // (app, param1, param2, etc.)
require("./routes/rofertas.js")(app,swig,gestorBD); // (app, param1, param2, etc.)
require("./routes/rapiofertas.js")(app, gestorBD);


//URL principal es identificarse
app.get('/', function (req, res) {
    res.redirect('/identificarse');
})

//Manejo de errores
app.use(function (err, req, res, next){
    console.log("Error producido: "+ err); //mostramos es el error en cosola
    if (! res.headersSent) {
        res.status(400);
        res.send("Recurso no disponible");
    }
});

//Lanzar el servidor
https.createServer({
    key: fs.readFileSync('certificates/alice.key'),
    cert: fs.readFileSync('certificates/alice.crt')
}, app).listen(app.get('port'), function() {
    console.log("Servidor activo");
});