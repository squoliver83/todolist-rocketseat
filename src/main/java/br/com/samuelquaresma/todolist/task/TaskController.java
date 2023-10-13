package br.com.samuelquaresma.todolist.task;

import br.com.samuelquaresma.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository repository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) { // 10/11/23 > 10/10/23, por exemplo
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Data de início e de término deve ser maior que a data atual");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) { // 10/11/23 > 10/10/23, por exemplo
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Data de início deve ser menor que a data de término");
        }

        return ResponseEntity.status(HttpStatus.OK).body(repository.save(taskModel));
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        return repository.findByIdUser((UUID) request.getAttribute("idUser"));
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {

        var task = repository.findById(id).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tarefa não encontrada");
        }

        var idUser = request.getAttribute("idUser");
        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alterar essa tarefa");
        }

        Utils.copyNonNullProperties(taskModel, task);

        return ResponseEntity.ok().body(repository.save(task));
    }
}
