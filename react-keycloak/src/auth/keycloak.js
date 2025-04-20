// keycloak.js
import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
    url: 'http://localhost:9000',
    realm: 'test_realm',
    clientId: 'client-dev'

});
export default keycloak;
