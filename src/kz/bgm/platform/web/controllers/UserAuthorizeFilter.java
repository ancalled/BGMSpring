package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.domain.AdminUser;
import kz.bgm.platform.model.domain.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class UserAuthorizeFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect("/bgm/mvc/main/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        AdminUser adminUser = (AdminUser) session.getAttribute("admin");
        if (adminUser == null) {
            resp.sendRedirect("/bgm/mvc/main/login");
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    @Override
    public void destroy() {

    }
}
