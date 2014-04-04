/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.server;

import net.wessendorf.keycloak.extended.EndpointOne;
import net.wessendorf.keycloak.extended.EndpointTwo;
import org.jboss.resteasy.logging.Logger;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ApplicationModel;
import org.keycloak.models.ClaimMask;
import org.keycloak.models.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.managers.ApplicationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;
import org.keycloak.util.JsonSerialization;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class KeycloakServerApplication extends KeycloakApplication {

    private static final Logger log = Logger.getLogger(KeycloakServerApplication.class);

    static {
        Config.setAdminRealm("UnifiedPushServer");
    }

    public KeycloakServerApplication(@Context ServletContext servletContext) throws FileNotFoundException {
        super(servletContext);

        // add the endpoints.......
        classes.add(EndpointOne.class);
        classes.add(EndpointTwo.class);



        //Config.setThemeDefault("ups");

        KeycloakSession session = factory.createSession();
        session.getTransaction().begin();



        try {
            RealmManager manager = new RealmManager(session);

            RealmModel adminRealm = manager.getRealm(Config.getAdminRealm());


            // No need to require admin to change password as this server is for dev/test
            adminRealm.getUser("admin").removeRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);

            if (adminRealm.getApplicationByName("my-app") == null) {

                //adminRealm.set

                // Create Application in realm for console and initialize it
                ApplicationModel consoleApp = new ApplicationManager(manager).createApplication(adminRealm, "my-app");

                // roles and scope:
                consoleApp.addDefaultRole("user");
                consoleApp.addRole("admin");
                consoleApp.addScope(adminRealm.getRole(AdminRoles.ADMIN));

                // do I really need this ????
                consoleApp.setAllowedClaimsMask(ClaimMask.USERNAME);


                // By default: is public, hence no credential/secret in keykloak.json. But we want that:
				consoleApp.setPublicClient(false);

                // TODO:
                // hrm......... how do I set the "" to false???



            }


            session.getTransaction().commit();
        } finally {
            session.close();
        }



    }

}

