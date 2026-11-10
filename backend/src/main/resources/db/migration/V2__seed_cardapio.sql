-- TROCAR pelos dados reais da hamburgueria.

INSERT INTO categorias (nome) VALUES
    ('Burgers'),
    ('Acompanhamentos'),
    ('Bebidas');

INSERT INTO produtos (nome, descricao, preco, categoria_id, disponivel) VALUES
    ('X-Relatos', 'Pao brioche, blend artesanal 180g, queijo cheddar, alface, tomate e molho da casa.', 32.90, 1, b'1'),
    ('X-Bacon', 'Blend artesanal 180g, cheddar, bacon crocante e molho barbecue.', 35.90, 1, b'1'),
    ('X-Salada', 'Blend artesanal 160g, queijo prato, alface, tomate e maionese artesanal.', 30.90, 1, b'1'),
    ('X-Tudo da Casa', 'Blend 200g, cheddar, bacon, ovo, presunto, alface, tomate e molho especial.', 39.90, 1, b'1'),
    ('Batata Frita', 'Porcao de batatas fritas crocantes com sal e pimenta.', 18.90, 2, b'1'),
    ('Onion Rings', 'Aneis de cebola empanados e crocantes com molho da casa.', 22.90, 2, b'0'),
    ('Coca-Cola Lata', 'Refrigerante Coca-Cola lata 350 ml.', 7.00, 3, b'1'),
    ('Suco de Laranja', 'Suco de laranja natural 400 ml.', 10.00, 3, b'1'),
    ('Milkshake de Chocolate', 'Milkshake cremoso de chocolate 400 ml.', 16.90, 3, b'1'),
    ('Agua Mineral', 'Agua mineral sem gas 500 ml.', 4.00, 3, b'1');

INSERT INTO informacoes_institucionais (nome, descricao, endereco, telefone, horario_funcionamento) VALUES
    ('Relatos Burger', 'Hamburgueria artesanal com blend proprio e ingredientes selecionados.', 'Av. das Palmeiras, 1234 - Setor Bueno, Goiania - GO', '(62) 3333-4444', 'Terca a domingo, das 18h as 23h');
