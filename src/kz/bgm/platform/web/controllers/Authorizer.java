package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/auth")
public class Authorizer {

    @Autowired
    private MainService mainService;

    @RequestMapping(value = "/logon", method = RequestMethod.GET)
    public String logon() {
        return "/login";

    }

    /*@RequestMapping(value = "logon", method = RequestMethod.POST)
    public String logon(
            HttpSession session,
            @RequestParam(value = "u") String login,
            @RequestParam(value = "p") String pass
    ) {

        if (login != null && pass != null) {
            AdminUser user = mainService.getAdmin(login, pass);
            if (user != null) {
                session.setAttribute("admin", user);

            } else {
                return "redirect:/admin-login.html?er=no-user-found";
            }
        }
        return "redirect:/mvc/main/index";
    }


    @RequestMapping(value = "logout", method = RequestMethod.POST)
    public String logout(HttpSession session) {
        AdminUser user = (AdminUser) session.getAttribute("admin");

        if (user != null) {
            session.setAttribute("admin", null);
        }

        return "redirect:/admin-login.html";
    }
*/

}
