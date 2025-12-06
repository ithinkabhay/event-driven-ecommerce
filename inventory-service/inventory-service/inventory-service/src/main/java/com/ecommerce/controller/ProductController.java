package com.ecommerce.controller;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.impl.ImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ImageService imageService;

    public ProductController(ProductService productService, ImageService imageService) {
        this.productService = productService;
        this.imageService = imageService;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody CreateProductRequest request
            ){
        ProductResponse project = productService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProduct(){
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long id
    ){
       return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<ProductResponse> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file")MultipartFile file
            ) throws IOException{

        String imageUrl = imageService.uploadProductImage(id, file);
        ProductResponse update = productService.getById(id);
        return ResponseEntity.ok(update);
    }


}
