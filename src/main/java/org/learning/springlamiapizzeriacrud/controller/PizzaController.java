package org.learning.springlamiapizzeriacrud.controller;

import jakarta.validation.Valid;
import org.learning.springlamiapizzeriacrud.model.Pizza;
import org.learning.springlamiapizzeriacrud.repository.PizzaRepository;
import org.learning.springlamiapizzeriacrud.service.PizzaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pizzas")
public class PizzaController {
    @Autowired
    private PizzaRepository pizzaRepository;
    @Autowired
    private PizzaService pizzaService;

    @GetMapping
    public String index(Model model, @RequestParam(name = "name", required = false) Optional<String> name){
        if (name.isPresent()){
            List<Pizza> menu = pizzaService.getFilteredPizzas(name.get());
            model.addAttribute("list", menu);
            model.addAttribute("queryValue", name.get());
        } else {
            List<Pizza> menu = pizzaService.getAllPizzas();
            model.addAttribute("list", menu);
        }
        return "/pizzas/index";

    }

    @GetMapping("/{id}")
    public String show(Model model, @PathVariable("id") Integer id){

        try{
            Pizza pizza = pizzaService.getPizzaById(id);
            model.addAttribute("pizza", pizza);
        } catch (RuntimeException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This pizza is not in our database");
        }

        return "/pizzas/show";
    }
    @GetMapping("/create")
    public String create(Model model){
        model.addAttribute("pizza", new Pizza());
        return "/pizzas/editCreate";
    }
    @PostMapping("/create")
    public String createDBInstance(@Valid @ModelAttribute("pizza") Pizza form,  BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "/pizzas/editCreate";
        }
        pizzaService.createPizza(form);
        return "redirect:/pizzas";
    }
    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable("id") Integer id){
        try {
            Pizza pizza = pizzaService.getPizzaById(id);
            model.addAttribute("pizza", pizza);
            return "/pizzas/editCreate";
        } catch (RuntimeException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This pizza is not in our database");
        }

    }
    @PostMapping("/edit/{id}")
    public String editUpdate(@Valid @ModelAttribute("pizza") Pizza form,  BindingResult bindingResult, Model model,  @PathVariable("id") Integer id){
        if (!pizzaService.isUniqueName(form)){
            bindingResult.addError(new FieldError("pizza", "name", form.getName(), false, null, null, "Name must be unique"));
        }
        if(bindingResult.hasErrors()){
            return "/pizzas/editCreate";
        }

        try {
            Pizza pizza = pizzaService.updatePizza(form, id);
            return "redirect:/pizzas/"+ Integer.toString(pizza.getId());
        } catch (RuntimeException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This pizza is not in our database");
        }
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try{
            boolean deleted = pizzaService.deleteById(id);
            if (deleted){
                redirectAttributes.addFlashAttribute("message", "Pizza deleted");
            } else {
                redirectAttributes.addFlashAttribute("message", "Unable to delete this item");
            }
        }catch (Exception e){
                redirectAttributes.addFlashAttribute("message", "No Pizza founded");
        }
        return "redirect:/pizzas";
    }
}
