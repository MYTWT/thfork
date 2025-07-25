/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.ProductDAO;
import model.ProductDTO;
import utils.AuthUtils;

/**
 *
 * @author tungi
 */
@WebServlet(name = "ProductController", urlPatterns = {"/ProductController"})
public class ProductController extends HttpServlet {

    ProductDAO pdao = new ProductDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String url = "";
        try {
            String action = request.getParameter("action");
            if (action.equals("addProduct")) {
                url = handleProductAdding(request, response);
            } else if (action.equals("searchProduct")) {
                url = handleProductSearching(request, response);
            } else if (action.equals("changeProductStatus")) {
                url = handleProductStatusChanging(request, response);
            } else if (action.equals("editProduct")) {
                url = handleProductEditing(request, response);
            } else if (action.equals("updateProduct")) {
                url = handleProductUpdating(request, response);
            }
        } catch (Exception e) {
        } finally {
            request.getRequestDispatcher(url).forward(request, response);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String handleProductAdding(HttpServletRequest request, HttpServletResponse response) {
        if (AuthUtils.isAdmin(request)) {
            String checkError = "";
            String message = "";
            String id = request.getParameter("id");
            String name = request.getParameter("name");
            String image = request.getParameter("image");
            String description = request.getParameter("description");
            String price = request.getParameter("price");
            String size = request.getParameter("size");
            String status = request.getParameter("status");

            double price_value = 0;
            try {
                price_value = Double.parseDouble(price);
            } catch (Exception e) {
            }

            boolean status_value = true;
            try {
                status_value = Boolean.parseBoolean(status);
            } catch (Exception e) {
            }

            // Kiem tra loi
            if (pdao.isProductExists(id)) {
                checkError = "Product ID is aldready exists.";
            }
            if (price_value < 0) {
                checkError += "<br/> Price must be greater than zero.";
            }

            if (checkError.isEmpty()) {
                message = "Add product successfully.";
            }

            ProductDTO product = new ProductDTO(id, name, image, description, price_value, size, status_value);
            if (!pdao.create(product)) {
                checkError += "<br/>Can not add new product.";
            }

            request.setAttribute("product", product);
            request.setAttribute("checkError", checkError);
            request.setAttribute("message", message);

        }
        return "productForm.jsp";
    }

    private String handleProductSearching(HttpServletRequest request, HttpServletResponse response) {
        String keyword = request.getParameter("keyword");
        List<ProductDTO> list = pdao.getActiveProductsByName(keyword);
        request.setAttribute("list", list);
        request.setAttribute("keyword", keyword);
        return "welcome.jsp";
    }

    private String handleProductStatusChanging(HttpServletRequest request, HttpServletResponse response) {
        String productId = request.getParameter("productId");
        if (AuthUtils.isAdmin(request)) {
            pdao.updateStatus(productId, false);
        }
        return handleProductSearching(request, response);
    }

    private String handleProductEditing(HttpServletRequest request, HttpServletResponse response) {
        String productId = request.getParameter("productId");
        String keyword = request.getParameter("keyword");
        if (AuthUtils.isAdmin(request)) {
            ProductDTO product = pdao.getProductByID(productId);
            if (product != null) {
                request.setAttribute("keyword", keyword);
                request.setAttribute("product", product);
                request.setAttribute("isEdit", true);
                return "productForm.jsp";
            }
        }
        return handleProductSearching(request, response);
    }

    private String handleProductUpdating(HttpServletRequest request, HttpServletResponse response) {
        if (AuthUtils.isAdmin(request)) {
            String checkError = "";
            String message = "";
            String id = request.getParameter("id");
            String name = request.getParameter("name");
            String image = request.getParameter("image");
            String description = request.getParameter("description");
            String price = request.getParameter("price");
            String size = request.getParameter("size");
            String status = request.getParameter("status");

            double price_value = 0;
            try {
                price_value = Double.parseDouble(price);
            } catch (Exception e) {
                checkError += "Invalid price format.<br/>";
            }

            boolean status_value = status != null && status.equals("true");

            // Validation
            if (name == null || name.trim().isEmpty()) {
                checkError += "Product name is required.<br/>";
            }

            if (price_value <= 0) {
                checkError += "Price must be greater than zero.<br/>";
            }

            if (checkError.isEmpty()) {
                ProductDTO product = new ProductDTO(id, name, image, description, price_value, size, status_value);
                if (pdao.update(product)) {
                    message = "Product updated successfully.";
                    // Redirect to welcome page after successful update
                    return handleProductSearching(request, response);
                } else {
                    checkError += "Cannot update product.<br/>";
                    request.setAttribute("product", product);
                }
            } else {
                // Keep form data for user to correct
                ProductDTO product = new ProductDTO(id, name, image, description, price_value, size, status_value);
                request.setAttribute("product", product);
            }

            request.setAttribute("checkError", checkError);
            request.setAttribute("message", message);
            request.setAttribute("isEdit", true);
        }
        return "productForm.jsp";
    }

}
