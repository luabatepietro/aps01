package br.insper.loja.produto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping
    public Produto criarProduto(@RequestBody Produto produto) {
        return produtoService.criarProduto(produto);
    }

    @GetMapping("/{id}")
    public Produto buscarProdutoPorId(@PathVariable String id) {
        return produtoService.buscarProdutoPorId(id);
    }

    @GetMapping
    public List<Produto> listarProdutos() {
        return produtoService.listarProdutos();
    }

    @PutMapping("/{id}/decrementar")
    public Produto decrementarQuantidade(@PathVariable String id, @RequestParam int quantidade) {
        return produtoService.decrementarQuantidade(id, quantidade);
    }
}
