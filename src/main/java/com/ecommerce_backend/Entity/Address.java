package com.ecommerce_backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "address_seq"
    )
    @SequenceGenerator(
            name = "address_seq",
            sequenceName = "address_seq",
            initialValue = 50000001,
            allocationSize = 10
    )
    private Long addressId;

    @NotBlank
    @Size(min = 3, message = "Street name must be at least 3 characters")
    private String street;

    @NotBlank
    @Size(min = 2, message = "City name must be at least 2 characters")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State name must be at least 2 characters")
    private String state;

    @NotBlank
    @Size(min = 5, message = "Pincode must be at least 5 characters")
    private String pincode;

    @NotBlank
    @Size(min = 2, message = "Country name must be at least 2 characters")
    private String country;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private EcommUser user;
}
