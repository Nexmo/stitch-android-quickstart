var fs = require('fs');
var jwt = require('jsonwebtoken');


private_key = fs.readFileSync('private.key', 'utf8');

      var token = {
          "iss": "Chris Guzman",
          "iat": Math.floor(Date.now() / 1000) - 30,
          "nbf": Math.floor(Date.now() / 1000) - 30,
          "exp": Math.floor(Date.now() / 1000) + 3000,
          "jti": Date.now(),
          "sub": "jamie",
          "application_id": "b3d78d59-1800-4b07-9c7b-49e5c59a6426",
          "acl": {
            "paths": {
              "/**": {}
            }
          }
      }

      // Generated jwts will include an iat (issued at) claim by default,
      // unless noTimestamp is specified.
      var token = jwt.sign( token, {
              key: private_key
          }, {
              algorithm: 'RS256'
      });

console.log(token)
