package com.Monarca.Backend.service;

import com.Monarca.Backend.dto.ProductoDto;
import com.Monarca.Backend.dto.ProductoResponseDto;
import com.Monarca.Backend.dto.VarianteProductoResponseDto;
import com.Monarca.Backend.exception.RecursoNoEncontradoException;
import com.Monarca.Backend.model.Categoria;
import com.Monarca.Backend.model.ImagenProducto;
import com.Monarca.Backend.model.Producto;
import com.Monarca.Backend.model.VarianteProducto;
import com.Monarca.Backend.repository.CategoriaRepository;
import com.Monarca.Backend.repository.ImagenProductoRepository;
import com.Monarca.Backend.repository.ProductoRepository;
import com.Monarca.Backend.repository.VarianteProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Gestiona el ciclo de vida de los productos: alta, edición, baja lógica
 * y la vista de catálogo que consume el frontend.
 *
 * Cada producto tiene al menos una {@link VarianteProducto} (talla/color/stock)
 * y, opcionalmente, una {@link ImagenProducto} principal.
 */
@Service
public class ProductoService {

    private static final String TALLA_POR_DEFECTO = "ÚNICA";
    private static final String COLOR_POR_DEFECTO = "N/A";
    private static final String MARCA_POR_DEFECTO = "Monarca";
    private static final String DESCRIPCION_POR_DEFECTO = "Sin descripción";

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final VarianteProductoRepository varianteRepository;
    private final ImagenProductoRepository imagenRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           VarianteProductoRepository varianteRepository,
                           ImagenProductoRepository imagenRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.varianteRepository = varianteRepository;
        this.imagenRepository = imagenRepository;
    }

    // =========================================================
    // CONSULTAS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * Catálogo público: solo productos activos, convertidos a DTO para evitar
     * proxies LAZY de Hibernate y ciclos en el JSON.
     */
    @Transactional(readOnly = true)
    public List<ProductoResponseDto> listarCatalogo() {
        return productoRepository.findAll().stream()
                .filter(this::estaActivo)
                .map(this::convertirRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProductoResponseDto> buscarCatalogoPorId(Long id) {
        return productoRepository.findById(id)
                .filter(this::estaActivo)
                .map(this::convertirRespuesta);
    }

    // =========================================================
    // CREAR
    // =========================================================

    @Transactional
    public Producto guardar(ProductoDto dto) {
        validarDtoCreacion(dto);

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre().trim());
        producto.setCategoria(resolverCategoria(dto.getCategoria()));
        producto.setSlug(generarSlugUnico(dto.getNombre(), null));
        producto.setDescripcion(valorODefecto(dto.getDescripcion(), DESCRIPCION_POR_DEFECTO));
        producto.setMarca(valorODefecto(dto.getMarca(), MARCA_POR_DEFECTO));
        producto.setPrecioBase(dto.getPrecioBase() != null ? dto.getPrecioBase() : dto.getPrecio());
        producto.setActivo(true);
        producto.setDestacado(false);
        producto = productoRepository.save(producto);

        VarianteProducto variante = nuevaVariante(producto);
        variante.setTalla(valorODefecto(dto.getTalla(), TALLA_POR_DEFECTO));
        variante.setColor(valorODefecto(dto.getColor(), COLOR_POR_DEFECTO));
        variante.setColorHex(dto.getColorHex());
        variante.setPrecio(dto.getPrecio());
        variante.setStock(dto.getStock() != null ? dto.getStock() : 0);
        variante.setStockMinimo(dto.getStockMinimo() != null ? dto.getStockMinimo() : 0);
        varianteRepository.save(variante);

        if (tieneTexto(dto.getImg())) {
            guardarImagenPrincipal(producto, dto.getImg());
        }
        return producto;
    }

    // =========================================================
    // ACTUALIZAR (solo cambia los campos enviados)
    // =========================================================

    @Transactional
    public Producto actualizar(Long id, ProductoDto dto) {
        validarDtoActualizacion(dto);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        aplicarCambiosProducto(producto, dto);
        producto = productoRepository.save(producto);

        VarianteProducto variante = seleccionarVariante(producto, dto);
        aplicarCambiosVariante(variante, dto, producto.getPrecioBase());
        varianteRepository.save(variante);

        if (tieneTexto(dto.getImg())) {
            guardarImagenPrincipal(producto, dto.getImg());
        }
        return producto;
    }

    private void aplicarCambiosProducto(Producto producto, ProductoDto dto) {
        if (tieneTexto(dto.getNombre())) {
            producto.setNombre(dto.getNombre().trim());
            producto.setSlug(generarSlugUnico(dto.getNombre(), producto.getIdProducto()));
        }
        if (tieneTexto(dto.getCategoria())) {
            producto.setCategoria(resolverCategoria(dto.getCategoria()));
        }
        if (dto.getDescripcion() != null) {
            producto.setDescripcion(dto.getDescripcion());
        }
        if (tieneTexto(dto.getMarca())) {
            producto.setMarca(dto.getMarca().trim());
        }
        if (dto.getPrecioBase() != null) {
            producto.setPrecioBase(dto.getPrecioBase());
        }
    }

    /**
     * Elige qué variante se edita, bloqueándola para escritura:
     * la indicada por id, la única existente, la que coincide en talla y color,
     * o una nueva si el producto aún no tiene variantes.
     */
    private VarianteProducto seleccionarVariante(Producto producto, ProductoDto dto) {
        Long idProducto = producto.getIdProducto();

        if (dto.getIdVariante() != null) {
            return varianteRepository.findByIdForUpdate(dto.getIdVariante())
                    .filter(v -> v.getProducto().getIdProducto().equals(idProducto))
                    .orElseThrow(() -> new IllegalArgumentException("La variante no pertenece al producto"));
        }

        List<VarianteProducto> variantes = varianteRepository.findByProducto_IdProducto(idProducto);
        if (variantes.isEmpty()) {
            return nuevaVariante(producto);
        }

        VarianteProducto elegida = variantes.size() == 1
                ? variantes.get(0)
                : buscarVarianteCoincidente(variantes, dto);
        return varianteRepository.findByIdForUpdate(elegida.getIdVariante()).orElseThrow();
    }

    private void aplicarCambiosVariante(VarianteProducto variante, ProductoDto dto, BigDecimal precioBase) {
        if (tieneTexto(dto.getTalla())) {
            variante.setTalla(dto.getTalla().trim());
        } else if (variante.getTalla() == null) {
            variante.setTalla(TALLA_POR_DEFECTO);
        }

        if (tieneTexto(dto.getColor())) {
            variante.setColor(dto.getColor().trim());
        } else if (variante.getColor() == null) {
            variante.setColor(COLOR_POR_DEFECTO);
        }

        if (dto.getColorHex() != null) {
            variante.setColorHex(dto.getColorHex());
        }

        if (dto.getPrecio() != null) {
            variante.setPrecio(dto.getPrecio());
        } else if (variante.getPrecio() == null) {
            variante.setPrecio(precioBase);
        }

        if (dto.getStock() != null) {
            variante.setStock(dto.getStock());
        } else if (variante.getStock() == null) {
            variante.setStock(0);
        }

        if (dto.getStockMinimo() != null) {
            variante.setStockMinimo(dto.getStockMinimo());
        } else if (variante.getStockMinimo() == null) {
            variante.setStockMinimo(0);
        }
    }

    private VarianteProducto buscarVarianteCoincidente(List<VarianteProducto> variantes, ProductoDto dto) {
        if (dto.getTalla() == null || dto.getColor() == null) {
            throw new IllegalArgumentException("El producto tiene varias variantes. Debes indicar talla y color.");
        }
        return variantes.stream()
                .filter(v -> dto.getTalla().equalsIgnoreCase(v.getTalla())
                        && dto.getColor().equalsIgnoreCase(v.getColor()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la variante indicada"));
    }

    // =========================================================
    // ELIMINAR (baja lógica: el producto queda inactivo)
    // =========================================================

    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    // =========================================================
    // AYUDANTES DE PERSISTENCIA
    // =========================================================

    private Categoria resolverCategoria(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada: " + nombre));
    }

    private VarianteProducto nuevaVariante(Producto producto) {
        VarianteProducto variante = new VarianteProducto();
        variante.setProducto(producto);
        variante.setSku(generarSku(producto));
        variante.setActivo(true);
        return variante;
    }

    /**
     * Crea o reemplaza la imagen principal del producto. Es una imagen general
     * (no ligada a una variante concreta).
     */
    private void guardarImagenPrincipal(Producto producto, String url) {
        ImagenProducto imagen = imagenRepository
                .findFirstByProducto_IdProductoAndPrincipalTrue(producto.getIdProducto())
                .orElseGet(ImagenProducto::new);
        imagen.setProducto(producto);
        imagen.setVariante(null);
        imagen.setUrl(url.trim());
        imagen.setTextoAlternativo(producto.getNombre());
        imagen.setPrincipal(true);
        imagen.setOrden(0);
        imagenRepository.save(imagen);
    }

    private ProductoResponseDto convertirRespuesta(Producto producto) {
        Long id = producto.getIdProducto();
        List<VarianteProductoResponseDto> variantes = varianteRepository
                .findByProducto_IdProductoAndActivoTrue(id).stream()
                .map(v -> new VarianteProductoResponseDto(v.getIdVariante(), v.getSku(),
                        v.getTalla(), v.getColor(), v.getColorHex(), v.getPrecio(), v.getStock()))
                .toList();
        String imagen = imagenRepository.findFirstByProducto_IdProductoAndPrincipalTrue(id)
                .map(ImagenProducto::getUrl)
                .orElse(null);
        String categoria = producto.getCategoria() == null ? null : producto.getCategoria().getNombre();
        return new ProductoResponseDto(id, producto.getNombre(), producto.getSlug(),
                producto.getDescripcion(), producto.getMarca(), producto.getPrecioBase(),
                producto.getDestacado(), categoria, imagen, variantes);
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarDtoCreacion(ProductoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Datos del producto requeridos");
        }
        if (!tieneTexto(dto.getNombre())) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (!tieneTexto(dto.getCategoria())) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        if (dto.getPrecio() == null || esNegativo(dto.getPrecio())) {
            throw new IllegalArgumentException("El precio no es válido");
        }
        validarStock(dto);
        validarImagen(dto.getImg());
        if (esNegativo(dto.getPrecioBase())) {
            throw new IllegalArgumentException("Precio base inválido");
        }
    }

    private void validarDtoActualizacion(ProductoDto dto) {
        validarImagen(dto.getImg());
        if (esNegativo(dto.getPrecioBase())) {
            throw new IllegalArgumentException("Precio base inválido");
        }
        if (esNegativo(dto.getPrecio())) {
            throw new IllegalArgumentException("Precio inválido");
        }
        validarStock(dto);
    }

    private void validarStock(ProductoDto dto) {
        if (dto.getStock() != null && dto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (dto.getStockMinimo() != null && dto.getStockMinimo() < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        }
    }

    /** Solo se aceptan URLs HTTPS absolutas (p. ej. Supabase Storage). */
    private void validarImagen(String imagen) {
        if (!tieneTexto(imagen)) {
            return;
        }
        try {
            URI uri = URI.create(imagen.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("La imagen debe ser una URL HTTPS válida");
        }
    }

    // =========================================================
    // UTILIDADES
    // =========================================================

    private String generarSku(Producto producto) {
        return "MON-" + producto.getIdProducto() + "-" + System.currentTimeMillis();
    }

    /** Convierte el nombre en un slug URL-safe y le agrega un sufijo si ya existe. */
    private String generarSlugUnico(String nombre, Long idActual) {
        String base = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (base.isBlank()) {
            base = "producto";
        }

        String slug = base;
        int contador = 2;
        while (slugOcupado(slug, idActual)) {
            slug = base + "-" + contador++;
        }
        return slug;
    }

    private boolean slugOcupado(String slug, Long idActual) {
        return idActual == null
                ? productoRepository.existsBySlug(slug)
                : productoRepository.existsBySlugAndIdProductoNot(slug, idActual);
    }

    private boolean estaActivo(Producto producto) {
        return Boolean.TRUE.equals(producto.getActivo());
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static boolean esNegativo(BigDecimal valor) {
        return valor != null && valor.signum() < 0;
    }

    private static String valorODefecto(String valor, String defecto) {
        return tieneTexto(valor) ? valor.trim() : defecto;
    }
}
