package br.com.samuelquaresma.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.samuelquaresma.todolist.user.UserModel;
import br.com.samuelquaresma.todolist.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private UserRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String servletPath = request.getServletPath();
        if (servletPath.startsWith("/tasks/")) {
            // pegar autenticação (usuário e senha)
            String auth = request.getHeader("Authorization");
            String authEncoded = auth.substring("Basic".length()).trim();
            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
            String authString = new String(authDecoded);
            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            // validar usuário
            UserModel user = repository.findByUsername(username);
            if (user == null) {
                response.sendError(401);
            } else {
                // validar senha
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (passwordVerify.verified) {
                    //segue viagem
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
