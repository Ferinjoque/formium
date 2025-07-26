package modelo.usuario;

import jakarta.persistence.*;
import modelo.pedido.Pedido;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa las credenciales y el rol de un usuario.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombreUsuario;

    /** Hash de la contraseña; nunca se almacena en texto plano. */
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.USER;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pedido> pedidos = new ArrayList<>();

    /** Constructor requerido por JPA. */
    protected Usuario() { }

    public Usuario(String nombreUsuario, String passwordHash) {
        this.nombreUsuario = nombreUsuario;
        this.passwordHash  = passwordHash;
    }

    /* ---------------- getters / setters ---------------- */

    public Long getId()                    { return id; }
    public String getNombreUsuario()       { return nombreUsuario; }
    public String getPasswordHash()        { return passwordHash; }
    public void   setPasswordHash(String h){ this.passwordHash = h; }

    public Rol getRol()                    { return rol; }
    public void setRol(Rol rol)            { this.rol = rol; }

    public List<Pedido> getPedidos()       { return pedidos; }

    /* ---------------- utilidades de rol ---------------- */

    public boolean esAdmin()       { return rol == Rol.ADMIN || rol == Rol.SUPER_ADMIN; }
    public boolean esSuperAdmin()  { return rol == Rol.SUPER_ADMIN; }
}
