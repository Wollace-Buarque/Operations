package dev.cromo29.operations.utilitys;

import dev.cromo29.durkcore.Inventory.Inv;
import dev.cromo29.durkcore.Util.MakeItem;
import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.durkcore.Util.TextAnimation;
import dev.cromo29.operations.OperationPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public class ScrollerInventory {

    // Nomes
    private String nextPageName = "<b>Próxima página";
    private String previousPageName = "<b>Página anterior";

    //HashMap onde fica os jogadores que estão visualizando o inventártio
    public Map<UUID, ScrollerInventory> users = new HashMap<>();
    private Map<String, Integer> task = new HashMap<>();

    //Páginas
    private List<Inv> pages = new ArrayList<>();
    private int currpage = 0;
    private int limit;

    //Colocar itens no meio.
    private boolean onMiddle;

    //Cancelar clique no inventário.
    private boolean cancelClick;

    //Ignorar clique no inventário do jogador.
    private boolean ignorePlayerInventoryClick;

    //Cancelar clique no inventário do jogador.
    private boolean cancelPlayerInventoryClick;

    //Ao executar isto, irá abrir um inventário paginado para o player especificado, contendo os itens da ArrayList
    public ScrollerInventory(List<ItemStack> itens, String name, Player player, int limit, boolean onMiddle, boolean ignorePlayerInventoryClick, boolean cancelPlayerInventoryClick, boolean cancelClick) {

        this.limit = limit;
        this.onMiddle = onMiddle;
        this.cancelClick = cancelClick;
        this.ignorePlayerInventoryClick = ignorePlayerInventoryClick;
        this.cancelPlayerInventoryClick = cancelPlayerInventoryClick;

        if (name == null || name.equals("") || name.equals(" "))
            name = "Sem nome";

        name = name.trim();
        name += " [" + currpage + "/" + pages.size() + "]";

        //Cria uma nova página em branco
        Inv page = getBlankPage(name);
        page.setIgnorePlayerInventoryClick(ignorePlayerInventoryClick, cancelPlayerInventoryClick);
        page.setCancelClick(cancelClick);

        //De acordo com os itens da ArrayList, adiciona os itens ao ScrollerInventory
        for (ItemStack item : itens) {

            //Se a página atual estiver cheia, adicione a página a ArrayList de páginas e crie uma nova página
            //para adicionar novos itens
            if (page.getItem(limit) != null && page.getItem(limit).getType() != Material.AIR) {
                pages.add(page);
                page = getBlankPage(name);
            }

            //Adiciona o item a página atual
            if (onMiddle)
                page.setInMiddle(item);
            else page.setItemToFirstEmpty(item);
        }
        pages.add(page);

        users.put(player.getUniqueId(), this);
    }

    //Ao executar isto, irá abrir um inventário paginado para o player especificado, contendo os itens da ArrayList com eventos ao clicar no item
    public ScrollerInventory(List<ItemStack> itens, String name, Player player, int limit, boolean onMiddle,
                             boolean ignorePlayerInventoryClick, boolean cancelPlayerInventoryClick, boolean cancelClick, Consumer<InventoryClickEvent> clickHandler) {

        this.limit = limit;
        this.onMiddle = onMiddle;
        this.cancelClick = cancelClick;
        this.ignorePlayerInventoryClick = ignorePlayerInventoryClick;
        this.cancelPlayerInventoryClick = cancelPlayerInventoryClick;

        if (name == null || name.equals("") || name.equals(" "))
            name = "Sem nome";

        name = name.trim();

        //Cria uma nova página em branco
        Inv page = getBlankPage(name);
        page.setIgnorePlayerInventoryClick(ignorePlayerInventoryClick, cancelPlayerInventoryClick);
        page.setCancelClick(cancelClick);

        //De acordo com os itens da ArrayList, adiciona os itens ao ScrollerInventory
        for (ItemStack item : itens) {

            //Se a página atual estiver cheia, adicione a página a ArrayList de páginas e crie uma nova página
            //para adicionar novos itens
            if (page.getItem(limit) != null && page.getItem(limit).getType() != Material.AIR) {
                pages.add(page);
                page = getBlankPage(name);
            }

            //Adiciona o item a página atual
            if (onMiddle)
                page.setInMiddle(item, clickHandler);
            else page.setItemToFirstEmpty(item, clickHandler);
        }
        pages.add(page);

        users.put(player.getUniqueId(), this);
    }

    //Isto cria uma página em branco com os botões (Próxima e Anterior)
    private Inv getBlankPage(String name) {
        //Cria o inventário
        Inv page = new Inv(54, name);

        //Cria o Item de 'Próxima Página'
        ItemStack nextPage = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = nextPage.getItemMeta();
        meta.setDisplayName(nextPageName);
        nextPage.setItemMeta(meta);

        //Cria o Item de 'Página Anterior'
        ItemStack prevPage = new ItemStack(Material.ARROW, 1);
        meta = prevPage.getItemMeta();
        meta.setDisplayName(previousPageName);
        prevPage.setItemMeta(meta);

        //Ambos setam os itens no inventário atual

        page.setItem(53, new MakeItem(Material.ARROW).setName(nextPageName).build(), this::onClick);

        page.setItem(45, new MakeItem(Material.ARROW).setName(previousPageName).build(), this::onClick);
        return page;
    }

    private void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Obtenha o inventário atual do scroller que o jogador está olhando, caso ele esteja olhando.

        if (!users.containsKey(player.getUniqueId())) return;
        ScrollerInventory inventory = users.get(player.getUniqueId());

        if (event.getCurrentItem() == null) return; //Se o item clicado for nullo, retorna.
        if (event.getCurrentItem().getItemMeta() == null) return; //Se o ItemMeta do item for nullo, retorna.
        if (event.getCurrentItem().getItemMeta().getDisplayName() == null)
            return; //Se o nome do item for nullo, retorna.

        //Se o item pressionado ser o botão de próxima página
        if (event.getCurrentItem().getItemMeta().getDisplayName().contains(TXT.parse(nextPageName))) {

            //Cancelar o evento
            event.setCancelled(true);

            //Se não houver página seguinte, não faça nada.
            if (inventory.currpage < inventory.pages.size() - 1) {

                //Existe a próxima página, ir para ela
                inventory.currpage += 1;

                //Abrir está página
                open(player);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            }

            //Se o item pressionado ser o botão de página anterior
        } else if (event.getCurrentItem().getItemMeta().getDisplayName().contains(TXT.parse(previousPageName))) {
            event.setCancelled(true);

            //Se o número da página for maior que 0 (Existe uma página anterior)
            if (inventory.currpage > 0) {

                //Ir para a página anterior
                inventory.currpage -= 1;

                //Abrir está página
                open(player);
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            }
        }
    }

    public void open(Player player) {
        Inv page = pages.get(currpage);

        page.open(player);

        if (task.containsKey(player.getName()))
            Bukkit.getServer().getScheduler().cancelTask(task.get(player.getName()));

        TextAnimation textAnimation = new TextAnimation(new TextAnimation.ColorScrollForward(
                "Coletador de premios [" + (currpage + 1) + "/" + pages.size() + "]",
                "<d>",
                "<5>",
                "<5>",
                "<d>"));


        task.put(player.getName(), Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(
                OperationPlugin.get(),
                () -> Utils.updateTitle(player, textAnimation.next()), 0, 2));

        page.addCloseHandler(inventoryCloseEvent -> {
            if (task.containsKey(player.getName()))
                Bukkit.getServer().getScheduler().cancelTask(task.get(player.getName()));
        });


        if ((currpage + 1) == 1) page.removeItem(45);
        if ((currpage + 1) == pages.size()) page.removeItem(53);
    }

    public void updateInventory(Player player, List<ItemStack> itens, Consumer<InventoryClickEvent> clickHandler) {
        Inv page = getInventory(player);
        page.setIgnorePlayerInventoryClick(ignorePlayerInventoryClick, cancelPlayerInventoryClick);
        page.setCancelClick(cancelClick);

        String name = page.getInventory().getName();

        page.clearInventorySafe(53, 45);
        updateArrows(player);

        for (ItemStack item : itens) {

            //Se a página atual estiver cheia, adicione a página a ArrayList de páginas e crie uma nova página
            //para adicionar novos itens
            if (page.getItem(limit) != null && page.getItem(limit).getType() != Material.AIR) {
                pages.add(page);
                page = getBlankPage(name);
            }

            //Adiciona o item a página atual
            if (onMiddle) page.setInMiddle(item, clickHandler);
            else page.setItemToFirstEmpty(item, clickHandler);

        }
    }

    public void updateInventory(Player player, List<ItemStack> items) {
        Inv page = getInventory(player);
        page.setIgnorePlayerInventoryClick(ignorePlayerInventoryClick, cancelPlayerInventoryClick);
        page.setCancelClick(cancelClick);

        String name = page.getInventory().getName();

        page.clearInventorySafe(53, 45);
        updateArrows(player);

        for (ItemStack item : items) {

            //Se a página atual estiver cheia, adicione a página a ArrayList de páginas e crie uma nova página
            //para adicionar novos itens
            if (page.getItem(limit) != null && page.getItem(limit).getType() != Material.AIR) {
                pages.add(page);
                page = getBlankPage(name);
            }

            //Adiciona o item a página atual
            if (onMiddle) page.setInMiddle(item);
            else page.setItemToFirstEmpty(item);

        }
    }

    public void updateArrows(Player player) {
        Inv inv = getInventory(player);

        if (inv.getItem(53) != null && inv.getItem(53).getType() == Material.ARROW)
            inv.updateItem(53, new MakeItem(Material.ARROW).setName(nextPageName).build());

        if (inv.getItem(45) != null && inv.getItem(45).getType() == Material.ARROW)
            inv.updateItem(45, new MakeItem(Material.ARROW).setName(previousPageName).build());
    }

    public Inv getInventory(Player player) {
        return users.get(player.getUniqueId()).pages.get(currpage);
    }

    public String getNextPageName() {
        return nextPageName;
    }

    public String getPreviousPageName() {
        return previousPageName;
    }

    public int getCurrpage() {
        return currpage;
    }

    public List<Inv> getPages() {
        return pages;
    }

    public Map<UUID, ScrollerInventory> getUsers() {
        return users;
    }

    public void setNextPageName(String nextPageName) {
        this.nextPageName = nextPageName;
    }

    public void setPreviousPageName(String previousPageName) {
        this.previousPageName = previousPageName;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOnMiddle(boolean onMiddle) {
        this.onMiddle = onMiddle;
    }

    public void setCancelClick(boolean cancelClick) {
        this.cancelClick = cancelClick;
    }

    public void setIgnorePlayerInventoryClick(boolean ignorePlayerInventoryClick) {
        this.ignorePlayerInventoryClick = ignorePlayerInventoryClick;
    }

    public void setCancelPlayerInventoryClick(boolean cancelPlayerInventoryClick) {
        this.cancelPlayerInventoryClick = cancelPlayerInventoryClick;
    }
}
