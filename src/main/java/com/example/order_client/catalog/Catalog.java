package com.example.order_client.catalog;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catalogo fixo de produtos usado pelo simulador e pelo formulario do front-end. */
@RestController
@RequestMapping("/api/catalog")
public class Catalog {

  public record Product(String id, String name, double price, String emoji) {}

  public static final List<Product> PRODUCTS =
      List.of(
          new Product("kb-01", "Teclado Mecanico", 389.90, "⌨️"),
          new Product("ms-02", "Mouse Gamer", 189.90, "🖱️"),
          new Product("mn-03", "Monitor 27\"", 1499.00, "🖥️"),
          new Product("hs-04", "Headset", 299.90, "🎧"),
          new Product("wc-05", "Webcam Full HD", 249.00, "📷"),
          new Product("ch-06", "Cadeira Ergonomica", 1299.00, "🪑"),
          new Product("ss-07", "SSD 1TB", 459.90, "💾"),
          new Product("cf-08", "Cafe Especial 1kg", 89.90, "☕"));

  @GetMapping
  public List<Product> list() {
    return PRODUCTS;
  }
}
