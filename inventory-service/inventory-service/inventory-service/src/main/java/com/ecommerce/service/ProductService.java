package com.ecommerce.service;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    public ProductResponse createProject(CreateProductRequest request);
    public ProductResponse getById(Long id);
    public List<ProductResponse> getAll();
}
