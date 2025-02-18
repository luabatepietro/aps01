package br.insper.loja.evento;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.logging.Logger;

@Service
public class EventoService {

    private static final Logger logger = Logger.getLogger(EventoService.class.getName());

    public void salvarEvento(String usuario, String acao) {
        Evento evento = new Evento();
        evento.setEmail(usuario);
        evento.setAcao(acao);

        logger.info("Salvando evento para o usuário: " + usuario);
        logger.info("Ação: " + acao);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // Corrija a URL para apontar para a aplicação usuarioApplication na porta 80
            restTemplate.postForEntity(
                    "http://localhost:80/api/evento", evento, Evento.class);
            logger.info("Evento salvo com sucesso.");
        } catch (Exception e) {
            logger.severe("Erro ao salvar evento: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar evento", e);
        }
    }
}