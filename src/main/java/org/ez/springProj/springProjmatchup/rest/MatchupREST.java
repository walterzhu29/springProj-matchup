package org.ez.springProj.springProjmatchup.rest;

import com.google.api.client.auth.oauth2.Credential;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;


@RestController
@RequestMapping(value = "Matchup")
public class MatchupREST {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchupREST.class);

    public static Credential authorize() throws PolicyUtils.IO {
        InputStream in = 
    }

}
