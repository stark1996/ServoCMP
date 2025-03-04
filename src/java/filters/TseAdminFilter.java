package filters;

import Encryption.TrippleDes;
import Exceptions.MyLogger;
import globals.User;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TseAdminFilter implements Filter {

    private static final boolean debug = true;

    private FilterConfig filterConfig = null;

    public TseAdminFilter() {
    }

    private void doBeforeProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("TseAdminFilter:DoBeforeProcessing");
        }
    }

    private void doAfterProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("TseAdminFilter:DoAfterProcessing");
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        if (debug) {
            log("TseAdminFilter:doFilter()");
        }
        doBeforeProcessing(request, response);
        Throwable problem = null;
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            HttpSession session = req.getSession(false);

            if (session.getAttribute("SystemMaintenanceFlag") != null) {
                if ("1".equals(session.getAttribute("SystemMaintenanceFlag").toString())) {
                    res.sendRedirect("/ServoCMP/systemMaintenance.jsp");
                } else {
                    User user = (User) session.getAttribute("sUser");
                    String storedToken = (String)session.getAttribute("csrfToken");
                    String receivedToken = (String)(req.getHeader("X-CSRF-TOKEN")!=null?req.getHeader("X-CSRF-TOKEN"):req.getParameter("csrftoken"));
                    System.out.println("csrfToken at client servlet::"+ receivedToken);
                    System.out.println("csrfToken at server servlet::"+ storedToken);
//                    if(storedToken.equals(receivedToken)){
                        if(true){
                        if (user != null) {
                            if ("3".equals(user.getRole_id())) {
                                chain.doFilter(request, response);
                            } else {
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                            }
                        } else {
                            res.sendRedirect("/ServoCMP");
                        }
                    }
                }
            } else {
                res.sendRedirect("/ServoCMP");
            }
        } catch (Throwable t) {
            problem = t;
        }
        doAfterProcessing(request, response);
        if (problem != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            sendProcessingError(problem, response);
        }
    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }

    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null && debug) {
            log("TseAdminFilter:Initializing filter");
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("TseAdminFilter()");
        }
        StringBuilder sb = new StringBuilder("TseAdminFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
                MyLogger.logIt(ex, "sendProcessingError()");
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
                MyLogger.logIt(ex, "sendProcessingError()");
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
            MyLogger.logIt(ex, "sendProcessingError()");
        }
        return stackTrace;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }

}
