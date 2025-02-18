package br.insper.loja.compra;

import br.insper.loja.evento.EventoService;
import br.insper.loja.produto.Produto;
import br.insper.loja.produto.ProdutoService;
import br.insper.loja.produto.ProdutoController;
import br.insper.loja.usuario.Usuario;
import br.insper.loja.usuario.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {
    private static final Logger logger = Logger.getLogger(CompraService.class.getName());

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EventoService eventoService;

    @Autowired
    private ProdutoService produtoService;

    public List<Compra> getCompras() {
        return compraRepository.findAll();
    }

    public Compra salvarCompra(Compra compra) {
        logger.info("Iniciando processo de salvar compra...");

        // Busca o usuário pelo email
        logger.info("Buscando usuário: " + compra.getUsuario());
        Usuario usuario = usuarioService.getUsuario(compra.getUsuario());
        if (usuario == null) {
            logger.severe("Usuário não encontrado: " + compra.getUsuario());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
        logger.info("Usuário encontrado: " + usuario.getNome());

        // Define o nome do usuário e a data da compra
        compra.setNome(usuario.getNome());
        compra.setDataCompra(LocalDateTime.now());
        logger.info("Data da compra definida: " + compra.getDataCompra());

        // Busca os produtos e calcula o total
        logger.info("Buscando produtos...");
        List<Produto> produtos = compra.getProdutos().stream()
                .map(produtoId -> {
                    logger.info("Buscando produto com ID: " + produtoId);
                    Produto produto = produtoService.buscarProdutoPorId(produtoId);
                    if (produto == null) {
                        logger.severe("Produto não encontrado: " + produtoId);
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado: " + produtoId);
                    }
                    logger.info("Produto encontrado: " + produto.getNome() + ", Preço: " + produto.getPreco());
                    return produto;
                })
                .toList();
        logger.info("Produtos encontrados: " + produtos.size());

        // Calcula o total e verifica o estoque
        double total = 0;
        for (Produto produto : produtos) {
            logger.info("Verificando estoque do produto: " + produto.getNome());
            if (produto.getQuantidade() < 1) {
                logger.severe("Produto sem estoque: " + produto.getNome());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto sem estoque: " + produto.getNome());
            }
            logger.info("Decrementando quantidade do produto: " + produto.getNome());
            produtoService.decrementarQuantidade(produto.getId(), 1);
            total += produto.getPreco(); // Adiciona o preço do produto ao total
            logger.info("Preço do produto adicionado ao total: " + produto.getPreco());
        }

        // Define o total da compra
        compra.setTotal(total);
        logger.info("Total da compra calculado: R$ " + total);

        // Salva o evento e a compra
        logger.info("Salvando evento de compra...");
        eventoService.salvarEvento(usuario.getEmail(), "Compra realizada. Total: R$ " + total);

        logger.info("Salvando compra no repositório...");
        Compra compraSalva = compraRepository.save(compra);
        logger.info("Compra salva com sucesso. ID: " + compraSalva.getId());

        return compraSalva;
    }

}
