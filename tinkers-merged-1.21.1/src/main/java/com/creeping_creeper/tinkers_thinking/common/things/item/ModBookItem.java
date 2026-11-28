package com.creeping_creeper.tinkers_thinking.common.things.item;

import com.creeping_creeper.tinkers_thinking.common.client.ModBooks;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import modernmods.hilt.client.SafeClientAccess;
import modernmods.hilt.item.LecternBookItem;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.tools.capability.inventory.InventorySlotMenuModule;

public class ModBookItem extends LecternBookItem {
   private static final Component CLICK_TO_OPEN = TConstruct.makeTranslation("item", "book.click_to_open")
      .withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.ITALIC});
   private final ModBookItem.BookType bookType;

   public ModBookItem(Properties props, ModBookItem.BookType bookType) {
      super(props);
      this.bookType = bookType;
   }

   public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
      Level world = context.level();
      if (world != null && world.isClientSide) {
         Player player = SafeClientAccess.getPlayer();
         if (player != null && InventorySlotMenuModule.isValidContainer(player.containerMenu)) {
            Inventory inventory = player.getInventory();
            if (inventory.items.contains(stack) || inventory.offhand.contains(stack)) {
               tooltip.add(CLICK_TO_OPEN);
            }
         }
      }

      super.appendHoverText(stack, context, tooltip, flagIn);
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (world.isClientSide) {
         ModBooks.getBook().openGui(hand, stack);
      }

      return new InteractionResultHolder(InteractionResult.SUCCESS, stack);
   }

   public void openLecternScreenClient(BlockPos pos, ItemStack stack) {
      ModBooks.getBook().openGui(pos, stack);
   }

   public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
      if (action == ClickAction.SECONDARY
         && held.isEmpty()
         && slot.container == player.getInventory()
         && slot.allowModification(player)
         && InventorySlotMenuModule.isValidContainer(player.containerMenu)) {
         if (player.level().isClientSide) {
            player.containerMenu.resumeRemoteUpdates();
            player.closeContainer();
            ModBooks.getBook().openGui(slot.getSlotIndex(), stack);
         }

         return true;
      } else {
         return false;
      }
   }

   public static enum BookType {
      FANTASTIC_GADGETRY;
   }
}
