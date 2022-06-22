package hwmsacapstoneteam.infra;

import hwmsacapstoneteam.domain.*;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/orders")
@Transactional
public class OrderController {

    @Autowired
    OrderRepository orderRepository;
    // keep

    @GetMapping(value = "/memleak")
    public String Memleak() {
        String[] S = new String[1000];
        int i = 0 , j = 0;
        
        for (i = 0;i < 1000; i++) {
            S[i] = new String();
            for (j = 0; j < 1000; j++) {
                S[i] += "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            }
        }
        return "Memleak Test";
    }
}
