package com.boardinghouse.controller;

import com.boardinghouse.service.PropertyService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Validated
public class PropertyController {
    private final PropertyService properties;
    public PropertyController(PropertyService properties) { this.properties = properties; }

    @GetMapping("/properties")
    public String list(Authentication auth, Model model) {
        model.addAttribute("properties", properties.listProperties(auth.getName()));
        model.addAttribute("username", auth.getName());
        return "properties";
    }

    @PostMapping("/properties")
    public String create(@RequestParam @NotBlank @Size(max=120) String name,
            @RequestParam(required=false) @Size(max=255) String address, Authentication auth,
            RedirectAttributes redirect) {
        try { return "redirect:/properties/" + properties.createProperty(auth.getName(), name, address); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); return "redirect:/properties"; }
    }

    @GetMapping("/properties/{propertyId}")
    public String detail(@PathVariable Long propertyId, Authentication auth, Model model) {
        model.addAttribute("property", properties.getProperty(auth.getName(), propertyId));
        model.addAttribute("username", auth.getName());
        return "property-editor";
    }

    @PostMapping("/properties/{propertyId}/floors")
    public String addFloor(@PathVariable Long propertyId, @RequestParam @Min(0) int floorNumber,
            Authentication auth, RedirectAttributes redirect) {
        try { properties.addFloor(auth.getName(), propertyId, floorNumber); redirect.addFlashAttribute("notice", "Floor added."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/properties/" + propertyId;
    }

    @PostMapping("/properties/{propertyId}/rooms")
    public String addRoom(@PathVariable Long propertyId, @RequestParam Long floorId,
            @RequestParam @NotBlank @Size(max=30) String roomNumber, @RequestParam @Min(1) int capacity,
            Authentication auth, RedirectAttributes redirect) {
        try { properties.addRoom(auth.getName(), propertyId, floorId, roomNumber, capacity); redirect.addFlashAttribute("notice", "Room added."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/properties/" + propertyId;
    }

    @PostMapping("/properties/{propertyId}/tenants")
    public String addTenant(@PathVariable Long propertyId, @RequestParam @NotBlank @Size(max=120) String fullName,
            @RequestParam(required=false) @Size(max=30) String phone, @RequestParam(required=false) @Size(max=120) String email,
            @RequestParam(defaultValue="0.00") BigDecimal monthlyRent,
            Authentication auth, RedirectAttributes redirect) {
        try { properties.addTenant(auth.getName(), propertyId, fullName, phone, email, monthlyRent); redirect.addFlashAttribute("notice", "Tenant added."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/properties/" + propertyId;
    }

    @PostMapping("/properties/{propertyId}/tenants/{tenantId}/edit")
    public String editTenant(@PathVariable Long propertyId, @PathVariable Long tenantId,
            @RequestParam @NotBlank @Size(max=120) String fullName,
            @RequestParam(required=false) @Size(max=30) String phone,
            @RequestParam(required=false) @Size(max=120) String email,
            @RequestParam BigDecimal monthlyRent, Authentication auth, RedirectAttributes redirect) {
        try {
            properties.updateTenant(auth.getName(), propertyId, tenantId, fullName, phone, email, monthlyRent);
            redirect.addFlashAttribute("notice", "Tenant details updated.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/properties/" + propertyId;
    }

    @PostMapping("/properties/{propertyId}/tenants/{tenantId}/delete")
    public String deleteTenant(@PathVariable Long propertyId, @PathVariable Long tenantId,
            Authentication auth, RedirectAttributes redirect) {
        try {
            properties.deleteTenant(auth.getName(), propertyId, tenantId);
            redirect.addFlashAttribute("notice", "Tenant deleted.");
        } catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/properties/" + propertyId;
    }

    @PostMapping("/properties/{propertyId}/assignments")
    public String assign(@PathVariable Long propertyId, @RequestParam Long tenantId,
            @RequestParam(required=false) Long roomId, Authentication auth, RedirectAttributes redirect) {
        try { properties.assignTenant(auth.getName(), propertyId, tenantId, roomId); redirect.addFlashAttribute("notice", roomId == null ? "Tenant unassigned." : "Room assignment saved."); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/properties/" + propertyId;
    }
}
