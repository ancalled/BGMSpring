package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.AdminService;
import kz.bgm.platform.model.service.MainService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    private static final Logger log = Logger.getLogger(AdminController.class);

    @Autowired
    private MainService mainService;

    @Autowired
    private AdminService adminService;


    @RequestMapping(value = "/customers")
    public String showCustomers(Model model) {
        List<Customer> customers = adminService.getAllCustomers();
        model.addAttribute("customers", customers);

        return "/admin/customers";
    }


    @RequestMapping(value = "/add-catalog", method = RequestMethod.GET)
    public String showAddCatalog(Model model,
                                 @RequestParam(value = "platformId", required = true) long platformId
    ) {

        Platform platform = mainService.getPlatform(platformId);
        model.addAttribute("platform", platform);

        return "/admin/add-catalog";
    }


    @RequestMapping(value = "/add-platform", method = RequestMethod.GET)
    public String showAddPlatform() {
        return "/admin/add-platform";
    }


    @RequestMapping(value = "/customer", method = RequestMethod.GET)
    public String showCustomerDetail(Model model,
                                     @RequestParam(value = "cid") long id
    ) {
        Customer customer = adminService.getCustomer(id);
        List<User> users = adminService.getUsersByCustomerId(id);

        model.addAttribute("customer", customer);
        model.addAttribute("users", users);

        return "admin/customer";
    }

    @RequestMapping(value = "/create-customer-form", method = RequestMethod.GET)
    public String showCreateCustomerForm() {
        return "/admin/create-customer-form";
    }


    @RequestMapping(value = "/create-user-form", method = RequestMethod.GET)
    public String showCreateUserForm(Model model,
                                     @RequestParam(value = "cid") long customerId) {

        Customer customer = adminService.getCustomer(customerId);
        model.addAttribute("customer", customer);

        return "/admin/create-user-form";
    }


    @RequestMapping(value = "/edit-track", method = RequestMethod.GET)
    public String showEditTrack(Model model,
                                @RequestParam(value = "id", required = true) long id) {

        Track track = mainService.getTrack(id);
        List<Catalog> catalogs = mainService.getCatalogs();

        model.addAttribute("track", track);
        model.addAttribute("catalogs", catalogs);

        return "/admin/edit-track";
    }


    @RequestMapping(value = "/create-platform", method = RequestMethod.POST)
    public String createPlatform(
            @RequestParam(value = "name", required = true) String name
    ) {
        Platform platform = new Platform();
        platform.setName(name);
        platform.setRights(true);
        adminService.createPlatform(platform);

        return "redirect:/mvc/main/index";
    }


    @RequestMapping(value = "/create-catalog", method = RequestMethod.POST)
    public String createCatalog(
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "royal", required = true) float royalty,
            @RequestParam(value = "rights", required = true) RightType rightType,
            @RequestParam(value = "platformId", required = true) long platformId
    ) {
        Catalog catalog = new Catalog();
        catalog.setName(name);
        catalog.setRoyalty(royalty);
        catalog.setRightType(rightType);
        catalog.setPlatformId(platformId);
        adminService.createCatalog(catalog);

        return "redirect:/mvc/main/index";
    }


    @RequestMapping(value = "/create-customer", method = RequestMethod.POST)
    public String createCustomer(
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "rights", required = true) RightType rightType,
            @RequestParam(value = "authorRoyalty", required = false, defaultValue = "0.0") float authorRoyalty,
            @RequestParam(value = "relatedRoyalty", required = false, defaultValue = "0.0") float relatedRoyalty,
            @RequestParam(value = "relatedRoyalty", required = true) CustomerType type
    ) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setRightType(rightType);
        customer.setAuthorRoyalty(authorRoyalty);
        customer.setAuthorRoyalty(relatedRoyalty);
        customer.setCustomerType(type);
        adminService.createCustomer(customer);

        return "redirect:/mvc/admin/customers";
    }


    @RequestMapping(value = "/create-user", method = RequestMethod.POST)
    public String createUser(
            @RequestParam(value = "customer-id", required = true) long customerId,
            @RequestParam(value = "login", required = true) String name,
            @RequestParam(value = "pass", required = true) String pass,
            @RequestParam(value = "full_name", required = false) String fullName,
            @RequestParam(value = "email", required = false) String email
    ) {

        User user = new User();
        user.setCustomerId(customerId);
        user.setLogin(name);
        user.setPass(pass);
        user.setFullName(fullName);
        user.setEmail(email);
        adminService.createUser(user);

        return "redirect:/mvc/admin/customer?cid=" + customerId;
    }


    @RequestMapping(value = "/delete-user", method = RequestMethod.POST)
    public String deleteUser(
            @RequestParam(value = "user-id", required = true) long userId
    ) {
        adminService.removeUser(userId);
        return "redirect:/mvc/main/index";
    }


    @RequestMapping(value = "/delete-customer", method = RequestMethod.POST)
    public String deleteCustomer(
            @RequestParam(value = "customer-id", required = true) long customerId
    ) {
        adminService.removeCustomer(customerId);
        return "redirect:/mvc/main/index";
    }


    @RequestMapping(value = "/update-track", method = RequestMethod.POST)
    public String editTrack(
            @RequestParam(value = "id", required = true) long id,
            @RequestParam(value = "catalog", required = true) Integer catId,
            @RequestParam(value = "code", required = true) String code,
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "artist", required = false, defaultValue = "") String artist,
            @RequestParam(value = "composer", required = false, defaultValue = "") String composer,
            @RequestParam(value = "mobile-share", required = true) float shareMobile,
            @RequestParam(value = "public-share", required = true) float sharePublic
    ) {

        log.info("Updating track, id: " + id);

        Track track = new Track();
        track.setId(id);
        track.setCatalogId(catId);
        track.setCode(code);
        track.setName(name);
        track.setArtist(artist);
        track.setComposer(composer);
        track.setMobileShare(shareMobile);
        track.setPublicShare(sharePublic);
        adminService.updateTrack(track);

        return "redirect:/mvc/admin/edit-track?id=" + id;
    }


}
