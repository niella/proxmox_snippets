package com.morpheus.proxmox.ve.util

import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.response.ServiceResponse
import groovy.util.logging.Slf4j
import org.apache.http.entity.ContentType


@Slf4j
class ProxmoxComputeUtil {

    static final String API_BASE_PATH = "/api2/json"

    private static ServiceResponse getApiV2Token(String uid, String pwd, String baseUrl) {
        def path = "access/ticket"
        log.debug("getApiV2Token: path: ${path}")
        HttpApiClient client = new HttpApiClient()
        def rtn = new ServiceResponse(success: false)
        try {

            def encUid = URLEncoder.encode((String) uid, "UTF-8")
            def encPwd = URLEncoder.encode((String) pwd, "UTF-8")
            def bodyStr = "username=" + "$encUid" + "&password=$encPwd"

            HttpApiClient.RequestOptions opts = new HttpApiClient.RequestOptions(
                    headers: ['Content-Type':'application/x-www-form-urlencoded'],
                    body: bodyStr,
                    contentType: ContentType.APPLICATION_FORM_URLENCODED,
                    ignoreSSL: true
            )
            def results = client.callJsonApi(baseUrl,"${API_BASE_PATH}/${path}", opts, 'POST')

            //log.debug("callListApiV2 results: ${results.toMap()}")
            if(results?.success && !results?.hasErrors()) {
                rtn.success = true
                def tokenData = results.data.data
                rtn.data = [csrfToken: tokenData.CSRFPreventionToken, token: tokenData.ticket]

                //log.info("CSRF: $csrfToken, Token: $token")
            } else {
                rtn.success = false
                rtn.msg = "Error retrieving token: $results.data"
                log.error("Error retrieving token: $results.data")
            }
            return rtn
        } catch(e) {
            log.error "Error in getApiV2Token: ${e}", e
            rtn.msg = "Error in getApiV2Token: ${e}"
            rtn.success = false
        }
        return rtn
    }


}
