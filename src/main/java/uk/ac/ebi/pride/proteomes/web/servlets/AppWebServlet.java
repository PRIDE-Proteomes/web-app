package uk.ac.ebi.pride.proteomes.web.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;

public class AppWebServlet extends HttpServlet {
    private static String templateUrl;
    private static String propertiesLocation =
            "uk/ac/ebi/pride/proteomes/web/properties/proteomes.properties";

    static {
        Properties props = new Properties();
        InputStream propStream = AppWebServlet.class.getClassLoader()
                                    .getResourceAsStream(propertiesLocation);
        try {
            props.load(propStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        templateUrl = props.getProperty("template.service.url");
    }

    /**
     * Initialize the <code>AppWebServlet</code>
     * @param servletConfig The Servlet configuration passed in by the servlet container
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }

    /**
     * Performs an HTTP GET request
     * @param request The {@link javax.servlet.http.HttpServletRequest}
     * @param response The {@link javax.servlet.http.HttpServletResponse}
     */
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        URL url = new URL(templateUrl);

        // make post mode connection
        URLConnection urlc = url.openConnection();
        urlc.setDoOutput(true);
        urlc.setAllowUserInteraction(false);
        OutputStreamWriter wr = new OutputStreamWriter(urlc.getOutputStream());
        wr.write(getWebConfigurationJSON());
        wr.flush();

        // retrieve result
        BufferedReader br = new BufferedReader(new InputStreamReader(
                                            urlc.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        String html = sb.toString();
        html = html.replace("##lastInBody##", getLastInBody());
        response.getWriter().write(html);
    }

    private String getLastInBody(){
        return "<!-- OPTIONAL: include this if you want history support -->\n" +
                "    <iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1'\n" +
                "            style=\"position:absolute;width:0;height:0;border:0\"></iframe>\n" +
                "\n" +
                "\n" +
                "    <!-- RECOMMENDED if your web app will not function without JavaScript enabled -->\n" +
                "    <noscript>\n" +
                "        <div style=\"width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif\">\n" +
                "            Your web browser must have JavaScript enabled\n" +
                "            in order for this application to display correctly.\n" +
                "        </div>\n" +
                "    </noscript>";
    }

    private String getWebConfigurationJSON() throws IOException {
        StringBuilder sb = new StringBuilder();
        ServletContext servletContext = this.getServletContext();
        String pathContext = servletContext.getRealPath("WEB-INF/webconfig/config.json");
        FileInputStream fstream = new FileInputStream(pathContext);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;

        while ((strLine = br.readLine()) != null)   {
            sb.append(strLine);
        }
        return URLEncoder.encode(sb.toString(), "ASCII");
    }
}
